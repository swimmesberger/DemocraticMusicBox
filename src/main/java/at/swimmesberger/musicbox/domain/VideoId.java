package at.swimmesberger.musicbox.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class VideoId implements Serializable {
    @Column(name = "video_id")
    private String videoId;

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
}
