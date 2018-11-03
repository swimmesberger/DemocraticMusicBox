package at.swimmesberger.musicbox.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "dmb_video_processing")
public class VideoProcessingUnit implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(nullable = false)
    private VideoId videoId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VideoId getVideoid() {
        return videoId;
    }

    public void setVideoid(VideoId videoid) {
        this.videoId = videoid;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus status) {
        this.status = status;
    }
}
