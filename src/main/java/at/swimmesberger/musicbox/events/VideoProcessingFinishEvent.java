package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import at.swimmesberger.musicbox.service.processing.ProcessedVideo;

public class VideoProcessingFinishEvent extends VideoProcessingEvent {
    private final ProcessedVideo video;
    private final long processingId;

    public VideoProcessingFinishEvent(Object source, ProcessingStatus status, ProcessedVideo video, long processingId) {
        super(source, video.getSource().getId(), status, status);
        this.video = video;
        this.processingId = processingId;
    }

    public ProcessedVideo getVideo() {
        return video;
    }

    public long getProcessingId() {
        return processingId;
    }

    @Override
    public String toString() {
        return "VideoProcessingFinishEvent{" +
            "video=" + video +
            ", processingId=" + processingId +
            ", videoId=" + getVideoId() +
            ", currentStatus=" + getCurrentStatus() +
            ", targetStatus=" + getTargetStatus() +
            ", timestamp=" + getTimestamp() +
            ", source=" + getSource() +
            '}';
    }
}
