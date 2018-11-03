package at.swimmesberger.musicbox.domain;

import at.swimmesberger.musicbox.service.dto.VideoIdDTO;
import at.swimmesberger.musicbox.service.dto.VideoPlatform;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class VideoId implements Serializable {
    @Column(name = "video_id")
    private String videoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_platform")
    private VideoPlatform videoPlatform;

    public VideoId() {
    }

    public VideoId(String videoId, VideoPlatform videoPlatform) {
        this.videoId = videoId;
        this.videoPlatform = videoPlatform;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public VideoPlatform getVideoPlatform() {
        return videoPlatform;
    }

    public void setVideoPlatform(VideoPlatform videoPlatform) {
        this.videoPlatform = videoPlatform;
    }

    public VideoIdDTO toDTO(){
        return new VideoIdDTO(this.getVideoId(), this.getVideoPlatform());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoId)) return false;
        VideoId videoId1 = (VideoId) o;
        return Objects.equals(getVideoId(), videoId1.getVideoId()) &&
            getVideoPlatform() == videoId1.getVideoPlatform();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVideoId(), getVideoPlatform());
    }

    public static VideoId create(VideoIdDTO dto){
        final VideoId id = new VideoId();
        id.setVideoId(dto.getVideoId());
        id.setVideoPlatform(dto.getVideoPlatform());
        return id;
    }
}
