package at.swimmesberger.musicbox.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
}
