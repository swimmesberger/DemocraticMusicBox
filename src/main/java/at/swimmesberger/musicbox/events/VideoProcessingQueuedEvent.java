package at.swimmesberger.musicbox.events;

import at.swimmesberger.musicbox.domain.VideoProcessingUnit;
import at.swimmesberger.musicbox.domain.ProcessingStatus;

public class VideoProcessingQueuedEvent extends VideoProcessingEvent {
    private final VideoProcessingUnit vpu;
    public VideoProcessingQueuedEvent(Object source, VideoProcessingUnit vpu) {
        super(source, vpu.getVideoid().toDTO(), ProcessingStatus.PEDNING, ProcessingStatus.PEDNING);
        this.vpu = vpu;
    }

    public VideoProcessingUnit getProcessingUnit(){
        return this.vpu;
    }
}
