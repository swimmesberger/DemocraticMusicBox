package at.swimmesberger.musicbox.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessVideoDTO {
    private final String videoURI;

    @JsonCreator
    public ProcessVideoDTO(@JsonProperty("video_uri") String videoURI) {
        this.videoURI = videoURI;
    }

    @JsonProperty("video_uri")
    public String getVideoURI() {
        return videoURI;
    }
}
