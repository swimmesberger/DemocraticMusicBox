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

    //UTC unix timestamp
    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    //UTC unix timestamp
    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VideoId getVideoId() {
        return videoId;
    }

    public void setVideoId(VideoId videoid) {
        this.videoId = videoid;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus status) {
        this.status = status;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
