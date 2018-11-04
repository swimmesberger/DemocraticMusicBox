package at.swimmesberger.musicbox.service.processing;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import at.swimmesberger.musicbox.domain.Video;
import at.swimmesberger.musicbox.domain.VideoId;
import at.swimmesberger.musicbox.domain.VideoProcessingUnit;
import at.swimmesberger.musicbox.events.*;
import at.swimmesberger.musicbox.repository.VideoProcessingRepository;
import at.swimmesberger.musicbox.repository.VideoRepository;
import at.swimmesberger.musicbox.service.VideoIdProcessingService;
import at.swimmesberger.musicbox.service.dto.VideoIdDTO;
import at.swimmesberger.musicbox.service.dto.VideoReturnDTO;
import at.swimmesberger.musicbox.service.dto.VideoUnit;
import at.swimmesberger.musicbox.service.errors.UnsupportedVideoPlatformException;
import at.swimmesberger.musicbox.service.errors.VideoProcessingException;
import at.swimmesberger.musicbox.service.errors.VideoServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;


@Service
public class VideoProcessingService {
    private final Logger logger = LoggerFactory.getLogger(VideoProcessingService.class);
    private final YoutubeDlDriver dlDriver;
    private final VideoProcessingRepository processingRepository;
    private final VideoRepository videoRepository;
    private final VideoIdProcessingService idProcessingService;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ExecutorService processingExecutor;

    public VideoProcessingService(YoutubeDlDriver dlDriver, VideoRepository videoRepository, VideoProcessingRepository processingRepository, VideoIdProcessingService idProcessingService,
                                  TransactionTemplate transactionTemplate, ApplicationEventPublisher eventPublisher, @Value("#{blockingExecutor}") ExecutorService blockingExecutor) {
        this.dlDriver = dlDriver;
        this.processingRepository = processingRepository;
        this.videoRepository = videoRepository;
        this.idProcessingService = idProcessingService;
        this.transactionTemplate = transactionTemplate;
        this.eventPublisher = eventPublisher;
        this.processingExecutor = blockingExecutor;
    }

    @EventListener(ApplicationReadyEvent.class)
    protected void initEvent() {
        logger.info("Processing post processing units");
        this.transactionTemplate.execute(t -> {
            final List<VideoProcessingUnit> processingUnits = this.processingRepository.findByStatus(ProcessingStatus.PEDNING);
            this.processVideos(processingUnits);
            return null;
        });
    }

    @TransactionalEventListener(value = VideoProcessingQueuedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void videoQueuedEvent(VideoProcessingQueuedEvent event) {
        logger.info("Processing video processing queued event {}", event);
        this.transactionTemplate.execute(t -> {
            final Optional<VideoProcessingUnit> vpu = this.getProcessingUnit(event.getVideoProcessingUnitd());
            if(vpu.isPresent()) {
                this.processVideos(Collections.singleton(vpu.get()));
            }else{
                logger.info("VideoProcessingUnit not found for event {}", event);
            }
            return null;
        });
    }

    @EventListener(VideoProcessingFinishEvent.class)
    protected void videoProcessedEvent(VideoProcessingFinishEvent event) {
        this.transactionTemplate.execute(t -> {
            final ProcessedVideo processedVideo = event.getVideo();
            Video video = new Video();
            video.setId(VideoId.create(processedVideo.getSource().getId()));
            video.setTitle(processedVideo.getMetadata().getTitle());
            video.setDescription(processedVideo.getMetadata().getDescription());
            video.setVideoURI(processedVideo.getVideoURI().toString());
            video.setThumbnailURI(processedVideo.getThumbnailURI().toString());
            video = this.videoRepository.save(video);
            this.fireVideoCreatedEvent(video, event.getProcessingId());
            return null;
        });
    }

    public VideoProcessingUnit queueVideo(final VideoIdDTO videoIdDTO) {
        final long time = Instant.now().toEpochMilli();
        VideoProcessingUnit unit = new VideoProcessingUnit();
        unit.setCreatedAt(time);
        unit.setUpdatedAt(time);
        unit.setStatus(ProcessingStatus.PEDNING);
        unit.setVideoId(VideoId.create(videoIdDTO));
        unit = this.processingRepository.save(unit);
        this.fireQueuedEvent(unit);
        return unit;
    }

    public Optional<VideoProcessingUnit> getProcessingUnit(long id){
        return this.processingRepository.findById(id);
    }

    public List<VideoReturnDTO> getAllProcessedVideos(){
        final List<VideoProcessingUnit> processingUnits = this.processingRepository.findAllByOrderByCreatedAtAsc();
        return this.convertToVideoReturnDTO(processingUnits);
    }

    public List<VideoReturnDTO> getProcessedVideos(VideoIdDTO id){
        final VideoId baseId = VideoId.create(id);
        final List<VideoProcessingUnit> processingUnits = this.processingRepository.findAllByVideoIdOrderByCreatedAtAsc(baseId);
        return this.convertToVideoReturnDTO(processingUnits);
    }

    private List<VideoReturnDTO> convertToVideoReturnDTO(List<VideoProcessingUnit> processingUnits){
        final List<VideoReturnDTO> returnDTOS  = new ArrayList<>(processingUnits.size());
        for(final VideoProcessingUnit vpu : processingUnits){
            final VideoIdDTO idDTO = vpu.getVideoId().toDTO();
            returnDTOS.add(new VideoReturnDTO(idDTO, this.idProcessingService.createIdStringFromVideoId(idDTO), vpu.getId(), vpu.getUpdatedAt(), vpu.getStatus(), null));
        }
        return returnDTOS;
    }

    private void processVideos(final Collection<VideoProcessingUnit> vpus) {
        for (final VideoProcessingUnit vpu : vpus) {
            this.processingExecutor.submit(() -> {
                this.transactionTemplate.execute(t -> {
                    this.processVideoImpl(vpu);
                    return null;
                });
            });
        }
    }

    private void processVideoImpl(VideoProcessingUnit vpu) {
        final VideoId videoId = vpu.getVideoId();
        final VideoIdDTO videoIdDTO = videoId.toDTO();
        this.fireStartEvent(videoIdDTO);
        try {
            final VideoUnit videoUnit = this.idProcessingService.createVideoUnit(videoIdDTO);
            final ProcessedVideo processedVideo = this.dlDriver.downloadVideo(videoUnit, progress -> {
                this.fireProgressEvent(videoIdDTO, progress);
            });
            vpu.setStatus(ProcessingStatus.PROCESSED);
            vpu.setUpdatedAt(Instant.now().toEpochMilli());
            vpu = this.processingRepository.save(vpu);
            this.fireFinishEvent(processedVideo, vpu);
        } catch (VideoServiceException ex) {
            logger.error(ex.getMessage(), ex);
            this.fireErrorEvent(videoIdDTO, ex.getMessage(), 1);
        }
    }

    private void fireVideoCreatedEvent(Video video, long processingId) {
        this.eventPublisher.publishEvent(new VideoCreatedEvent(this, video.getId().toDTO(), processingId));
    }

    private void fireQueuedEvent(VideoProcessingUnit vpu) {
        this.eventPublisher.publishEvent(new VideoProcessingQueuedEvent(this, vpu.getVideoId().toDTO(), vpu.getId()));
    }

    private void fireStartEvent(VideoIdDTO id) {
        this.eventPublisher.publishEvent(new VideoProcessingStartEvent(this, id, ProcessingStatus.PEDNING, ProcessingStatus.PROCESSED));
    }

    private void fireProgressEvent(VideoIdDTO id, double progress) {
        this.eventPublisher.publishEvent(new VideoProcessingProgressEvent(this, id, ProcessingStatus.PEDNING, ProcessingStatus.PROCESSED, progress));
    }

    private void fireFinishEvent(ProcessedVideo video, VideoProcessingUnit vpu) {
        this.eventPublisher.publishEvent(new VideoProcessingFinishEvent(this, ProcessingStatus.PROCESSED, video, vpu.getId()));
    }

    private void fireErrorEvent(VideoIdDTO id, String errorMsg, int errorCode) {
        this.eventPublisher.publishEvent(new VideoProcessingErrorEvent(this, id, ProcessingStatus.PEDNING, ProcessingStatus.PROCESSED, errorMsg, errorCode));
    }
}
