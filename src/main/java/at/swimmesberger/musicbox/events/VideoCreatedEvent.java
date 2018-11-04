package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.service.dto.VideoIdDTO;
import org.springframework.context.ApplicationEvent;

public class VideoCreatedEvent extends ApplicationEvent {
    private final VideoIdDTO videoId;
    private final Long videoProcessingId;

    public VideoCreatedEvent(Object source, VideoIdDTO videoId, Long videoProcessingId) {
        super(source);
        this.videoId = videoId;
        this.videoProcessingId = videoProcessingId;
    }

    public Long getVideoProcessingId() {
        return videoProcessingId;
    }

    public VideoIdDTO getVideoId() {
        return videoId;
    }

    @Override
    public String toString() {
        return "VideoCreatedEvent{" +
            "videoId=" + videoId +
            ", videoProcessingId=" + videoProcessingId +
            ", source=" + source +
            ", timestamp=" + getTimestamp() +
            ", source=" + getSource() +
            '}';
    }
}
