package at.swimmesberger.musicbox.events;

import org.springframework.context.ApplicationEvent;

public class VideoPostProcessingCreated extends ApplicationEvent {
    //I don't want any jpa entities in an ApplicationEvent only primitive or immutable json serializable object are allowed
    private final long videoPostProcessingUnitId;

    public VideoPostProcessingCreated(Object source, long videoPostProcessingUnitId) {
        super(source);
        this.videoPostProcessingUnitId = videoPostProcessingUnitId;
    }

    public long getVideoPostProcessingUnitId() {
        return videoPostProcessingUnitId;
    }

    @Override
    public String toString() {
        return "VideoPostProcessingCreated{" +
            "videoPostProcessingUnit=" + this.getVideoPostProcessingUnitId() +
            ", timestamp=" + getTimestamp() +
            ", source=" + getSource() +
            '}';
    }
}
