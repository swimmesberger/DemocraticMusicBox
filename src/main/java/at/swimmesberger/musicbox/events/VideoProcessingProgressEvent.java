package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import at.swimmesberger.musicbox.service.dto.VideoIdDTO;

public class VideoProcessingProgressEvent extends VideoProcessingEvent {
    private final double progress;

    public VideoProcessingProgressEvent(Object source, VideoIdDTO videoId, ProcessingStatus currentStatus, ProcessingStatus targetStatus, double progress) {
        super(source, videoId, currentStatus, targetStatus);
        this.progress = progress;
    }

    public double getProgress() {
        return progress;
    }
}
