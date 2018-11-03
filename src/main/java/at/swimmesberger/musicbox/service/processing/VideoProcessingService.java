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
import at.swimmesberger.musicbox.service.dto.VideoUnit;
import at.swimmesberger.musicbox.service.errors.VideoServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
        this.transactionTemplate.execute(t -> {
            final List<VideoProcessingUnit> processingUnits = this.processingRepository.findByStatus(ProcessingStatus.PEDNING);
            this.processVideos(processingUnits);
            return null;
        });
    }

    @EventListener(VideoProcessingQueuedEvent.class)
    protected void videoQueuedEvent(VideoProcessingQueuedEvent event) {
        this.transactionTemplate.execute(t -> {
            this.processVideos(Collections.singleton(event.getProcessingUnit()));
            return null;
        });
    }

    @EventListener(VideoProcessingFinishEvent.class)
    protected void videoProcessedEvent(VideoProcessingFinishEvent event) {
        this.transactionTemplate.execute(t -> {
            final ProcessedVideo processedVideo = event.getVideo();
            final Video video = new Video();
            video.setId(VideoId.create(processedVideo.getSource().getId()));
            video.setTitle(processedVideo.getMetadata().getTitle());
            video.setDescription(processedVideo.getMetadata().getDescription());
            video.setVideoURI(processedVideo.getVideoURI().toString());
            video.setThumbnailURI(processedVideo.getThumbnailURI().toString());
            this.fireVideoCreatedEvent(this.videoRepository.save(video));
            return null;
        });
    }

    public VideoProcessingUnit queueVideo(final VideoIdDTO videoIdDTO) {
        VideoProcessingUnit unit = new VideoProcessingUnit();
        unit.setStatus(ProcessingStatus.PEDNING);
        unit.setVideoid(VideoId.create(videoIdDTO));
        unit = this.processingRepository.save(unit);
        this.fireQueuedEvent(unit);
        return unit;
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
        final VideoId videoId = vpu.getVideoid();
        final VideoIdDTO videoIdDTO = videoId.toDTO();
        this.fireStartEvent(videoIdDTO);
        try {
            final URI videoUri = this.idProcessingService.constructURI(videoId.toDTO());
            final ProcessedVideo processedVideo = this.dlDriver.downloadVideo(new VideoUnit(videoId.toDTO(), videoUri), progress -> {
                this.fireProgressEvent(videoIdDTO, progress);
            });
            vpu.setStatus(ProcessingStatus.PROCESSED);
            this.processingRepository.save(vpu);
            this.fireFinishEvent(processedVideo);
        } catch (VideoServiceException ex) {
            logger.error(ex.getMessage(), ex);
            this.fireErrorEvent(videoIdDTO, ex.getMessage(), 1);
        }
    }

    private void fireVideoCreatedEvent(Video video) {
        this.eventPublisher.publishEvent(new VideoCreatedEvent(this, video));
    }

    private void fireQueuedEvent(VideoProcessingUnit vpu) {
        this.eventPublisher.publishEvent(new VideoProcessingQueuedEvent(this, vpu));
    }

    private void fireStartEvent(VideoIdDTO id) {
        this.eventPublisher.publishEvent(new VideoProcessingStartEvent(this, id, ProcessingStatus.PEDNING, ProcessingStatus.PROCESSED));
    }

    private void fireProgressEvent(VideoIdDTO id, double progress) {
        this.eventPublisher.publishEvent(new VideoProcessingProgressEvent(this, id, ProcessingStatus.PEDNING, ProcessingStatus.PROCESSED, progress));
    }

    private void fireFinishEvent(ProcessedVideo video) {
        this.eventPublisher.publishEvent(new VideoProcessingFinishEvent(this, ProcessingStatus.PROCESSED, video));
    }

    private void fireErrorEvent(VideoIdDTO id, String errorMsg, int errorCode) {
        this.eventPublisher.publishEvent(new VideoProcessingErrorEvent(this, id, ProcessingStatus.PEDNING, ProcessingStatus.PROCESSED, errorMsg, errorCode));
    }
}
