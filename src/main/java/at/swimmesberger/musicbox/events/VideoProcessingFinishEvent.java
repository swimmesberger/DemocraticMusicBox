package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.domain.ProcessingStatus;
import at.swimmesberger.musicbox.service.processing.ProcessedVideo;

public class VideoProcessingFinishEvent extends VideoProcessingEvent {
    private final ProcessedVideo video;

    public VideoProcessingFinishEvent(Object source, ProcessingStatus status, ProcessedVideo video) {
        super(source, video.getSource().getId(), status, status);
        this.video = video;
    }

    public ProcessedVideo getVideo() {
        return video;
    }
}
