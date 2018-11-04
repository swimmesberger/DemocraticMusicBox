package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import at.swimmesberger.musicbox.service.dto.VideoIdDTO;

public class VideoProcessingErrorEvent extends VideoProcessingEvent {
    private final String errorMsg;
    private final int errorCode;

    public VideoProcessingErrorEvent(Object source, VideoIdDTO videoId, ProcessingStatus currentStatus, ProcessingStatus targetStatus, String errorMsg, int errorCode) {
        super(source, videoId, currentStatus, targetStatus);
        this.errorMsg = errorMsg;
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "VideoProcessingErrorEvent{" +
            "errorMsg='" + errorMsg + '\'' +
            ", errorCode=" + errorCode +
            ", videoId=" + getVideoId() +
            ", currentStatus=" + getCurrentStatus() +
            ", targetStatus=" + getTargetStatus() +
            ", timestamp=" + getTimestamp() +
            ", source=" + getSource() +
            '}';
    }
}
