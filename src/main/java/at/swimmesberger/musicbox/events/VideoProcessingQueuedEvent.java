package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import at.swimmesberger.musicbox.service.dto.VideoIdDTO;

public class VideoProcessingQueuedEvent extends VideoProcessingEvent {
    private final long vpuId;
    public VideoProcessingQueuedEvent(Object source, VideoIdDTO videoId, long vpuId) {
        super(source, videoId, ProcessingStatus.PEDNING, ProcessingStatus.PEDNING);
        this.vpuId = vpuId;
    }

    public long getVideoProcessingUnitd() {
        return vpuId;
    }

    @Override
    public String toString() {
        return "VideoProcessingQueuedEvent{" +
            "processingUnit=" + getVideoProcessingUnitd() +
            ", videoId=" + getVideoId() +
            ", currentStatus=" + getCurrentStatus() +
            ", targetStatus=" + getTargetStatus() +
            ", timestamp=" + getTimestamp() +
            ", source=" + getSource() +
            '}';
    }
}
