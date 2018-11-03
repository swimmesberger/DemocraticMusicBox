package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.domain.Video;
import org.springframework.context.ApplicationEvent;

public class VideoCreatedEvent extends ApplicationEvent {
    private final Video video;

    public VideoCreatedEvent(Object source, Video video) {
        super(source);
        this.video = video;
    }

    public Video getVideo() {
        return video;
    }
}
