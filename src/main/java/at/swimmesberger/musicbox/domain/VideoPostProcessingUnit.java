package at.swimmesberger.musicbox.domain;

import javax.persistence.*;
import java.io.Serializable;

/**
 * When a video is processed we have to execute some actions when that process has finished for this actions to survive restarts we persist them.
 * Adding a video to a playlist after a video is processed is a example for a VideopostPocessingUnit
 */
@Entity
@Table(name = "dmb_video_post_processing")
public class VideoPostProcessingUnit implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(nullable = false)
    private VideoId videoId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VideoPostProcessingType type;

    //a json string of data
    @Lob
    private String payload;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VideoId getVideoId() {
        return videoId;
    }

    public void setVideoId(VideoId videoId) {
        this.videoId = videoId;
    }

    public VideoPostProcessingType getType() {
        return type;
    }

    public void setType(VideoPostProcessingType type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus status) {
        this.status = status;
    }
}
