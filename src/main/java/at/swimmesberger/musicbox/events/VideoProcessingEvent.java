package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.domain.VideoId;
import at.swimmesberger.musicbox.domain.ProcessingStatus;
import at.swimmesberger.musicbox.service.dto.VideoIdDTO;
import org.springframework.context.ApplicationEvent;

public class VideoProcessingEvent extends ApplicationEvent {
    private final VideoIdDTO videoId;
    private final ProcessingStatus currentStatus;
    private final ProcessingStatus targetStatus;

    public VideoProcessingEvent(Object source, VideoIdDTO videoId, ProcessingStatus currentStatus, ProcessingStatus targetStatus) {
        super(source);
        this.videoId = videoId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public VideoIdDTO getVideoId() {
        return videoId;
    }

    public ProcessingStatus getCurrentStatus() {
        return currentStatus;
    }

    public ProcessingStatus getTargetStatus() {
        return targetStatus;
    }
}
