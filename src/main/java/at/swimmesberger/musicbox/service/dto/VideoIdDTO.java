package at.swimmesberger.musicbox.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class VideoIdDTO {
    private final String videoId;
    private final VideoPlatform videoPlatform;

    @JsonCreator
    public VideoIdDTO(@JsonProperty("video_id") String videoId, @JsonProperty("video_platform") VideoPlatform videoPlatform) {
        this.videoId = videoId;
        this.videoPlatform = videoPlatform;
    }

    @JsonProperty("video_id")
    public String getVideoId() {
        return videoId;
    }

    @JsonProperty("video_platform")
    public VideoPlatform getVideoPlatform() {
        return videoPlatform;
    }

    @Override
    public String toString() {
        return "VideoIdDTO{" +
            "videoId='" + videoId + '\'' +
            ", videoPlatform=" + videoPlatform +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoIdDTO that = (VideoIdDTO) o;
        return Objects.equals(getVideoId(), that.getVideoId()) &&
            getVideoPlatform() == that.getVideoPlatform();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVideoId(), getVideoPlatform());
    }
}
