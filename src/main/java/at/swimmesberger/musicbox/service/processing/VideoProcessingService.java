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
import at.swimmesberger.musicbox.service.errors.VideoServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;
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

    @EventListener(value = VideoProcessingQueuedEvent.class)
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

    @TransactionalEventListener(VideoProcessingFinishEvent.class)
    protected void videoProcessedEvent(VideoProcessingFinishEvent event) {
        try {
            final TransactionTemplate template = new TransactionTemplate(this.transactionTemplate.getTransactionManager(), this.transactionTemplate);
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            template.execute(t -> {
                final Optional<VideoProcessingUnit> vpuOp = this.processingRepository.findById(event.getProcessingId());
                if(!vpuOp.isPresent()){
                    logger.error("VideProcessingUnit with id {} not found!", event.getProcessingId());
                    return null;
                }

                final ProcessedVideo processedVideo = event.getVideo();
                Video video = new Video();
                video.setId(VideoId.create(processedVideo.getSource().getId()));
                video.setTitle(processedVideo.getMetadata().getTitle());
                video.setDescription(processedVideo.getMetadata().getDescription());
                video.setVideoURI(processedVideo.getVideoURI().toString());
                video.setThumbnailURI(processedVideo.getThumbnailURI().toString());
                video = this.videoRepository.save(video);

                VideoProcessingUnit vpu = vpuOp.get();
                vpu.setStatus(ProcessingStatus.PROCESSED);
                vpu.setUpdatedAt(Instant.now().toEpochMilli());
                vpu = this.processingRepository.save(vpu);

                this.fireVideoCreatedEvent(video, vpu.getId());
                return null;
            });
        }catch(DataIntegrityViolationException ex){
            //check if we added a video which exists already - could be a race condition
            if(ex.getMessage().contains("FK_VIDEO_PLATFORM_INDEX")){
                logger.warn("Video processing finish event but video exists already {}", event.getVideo());
                this.transactionTemplate.execute(t -> {
                    final Optional<Video> videoById = this.videoRepository.findById(VideoId.create(event.getVideoId()));
                    if(videoById.isPresent()){
                        this.fireVideoCreatedEvent(videoById.get(), event.getProcessingId());
                    }else{
                        logger.error("Violation exception without a video?!", ex);
                    }
                    return null;
                });
            }else{
                throw ex;
            }
        }
    }

    public void queueVideo(final VideoProcessingUnit unit) {
        this.fireQueuedEvent(unit);
    }

    public VideoProcessingUnit createProcesingVideo(final VideoIdDTO videoIdDTO){
        final long time = Instant.now().toEpochMilli();
        VideoProcessingUnit unit = new VideoProcessingUnit();
        unit.setCreatedAt(time);
        unit.setUpdatedAt(time);
        unit.setStatus(ProcessingStatus.PEDNING);
        unit.setVideoId(VideoId.create(videoIdDTO));
        unit = this.processingRepository.save(unit);
        return unit;
    }

    public VideoReturnDTO createReturnVideo(VideoProcessingUnit unit){
        final Map<VideoIdDTO, List<VideoReturnDTO>> processedVideosMap = this.getProcessedVideosMap(Collections.singletonList(unit));
        final List<VideoReturnDTO> returnVideos = processedVideosMap.get(unit.getVideoId().toDTO());
        if(returnVideos.size() <= 0)return null;
        return returnVideos.get(0);
    }

    public Optional<VideoProcessingUnit> getProcessingUnit(long id){
        return this.processingRepository.findById(id);
    }

    public Map<VideoIdDTO, List<VideoReturnDTO>> getProcessedVideos(){
        final List<VideoProcessingUnit> processingUnits = this.processingRepository.findAllByOrderByCreatedAtAsc();
        return this.getProcessedVideosMap(processingUnits);
    }

    public Map<VideoIdDTO, List<VideoReturnDTO>> getProcessedVideos(List<VideoIdDTO> ids){
        final List<VideoId> baseIds = VideoId.create(ids);
        final List<VideoProcessingUnit> processingUnits = this.processingRepository.findAllByVideoIdInOrderByCreatedAtAsc(baseIds);
        return this.getProcessedVideosMap(processingUnits);
    }

    private Map<VideoIdDTO, List<VideoReturnDTO>> getProcessedVideosMap(List<VideoProcessingUnit> processingUnits){
        final List<VideoReturnDTO> videos = this.convertToVideoReturnDTO(processingUnits);
        final Map<VideoIdDTO, List<VideoReturnDTO>> mappedVideos = new HashMap<>(videos.size());
        for(final VideoReturnDTO video : videos){
            if(!mappedVideos.containsKey(video.getId())){
                mappedVideos.put(video.getId(), new ArrayList<>());
            }
            mappedVideos.get(video.getId()).add(video);
        }
        return mappedVideos;
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
