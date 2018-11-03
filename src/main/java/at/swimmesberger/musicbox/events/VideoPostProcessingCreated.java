package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.domain.VideoPostProcessingUnit;
import org.springframework.context.ApplicationEvent;

public class VideoPostProcessingCreated extends ApplicationEvent {
    private final VideoPostProcessingUnit videoPostProcessingUnit;

    public VideoPostProcessingCreated(Object source, VideoPostProcessingUnit videoPostProcessingUnit) {
        super(source);
        this.videoPostProcessingUnit = videoPostProcessingUnit;
    }

    public VideoPostProcessingUnit getVideoPostProcessingUnit() {
        return videoPostProcessingUnit;
    }
}
