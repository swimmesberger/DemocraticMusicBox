package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import at.swimmesberger.musicbox.service.dto.VideoIdDTO;

public class VideoProcessingStartEvent extends VideoProcessingEvent {
    public VideoProcessingStartEvent(Object source, VideoIdDTO videoId, ProcessingStatus currentStatus, ProcessingStatus targetStatus) {
        super(source, videoId, currentStatus, targetStatus);
    }

    @Override
    public String toString() {
        return "VideoProcessingStartEvent{" +
            "videoId=" + getVideoId() +
            ", currentStatus=" + getCurrentStatus() +
            ", targetStatus=" + getTargetStatus() +
            ", timestamp=" + getTimestamp() +
            ", source=" + getSource() +
            '}';
    }
}
