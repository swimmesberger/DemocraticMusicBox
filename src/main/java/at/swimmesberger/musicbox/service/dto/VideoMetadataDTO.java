package at.swimmesberger.musicbox.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class VideoMetadataDTO {
    private final String title;
    private final String description;
    private final URI videoUri;
    private final URI thumbnailUri;

    @JsonCreator
    public VideoMetadataDTO(@JsonProperty("title") String title, @JsonProperty("description") String description, @JsonProperty("video_uri") URI videoUri, @JsonProperty("thumbnail_uri") URI thumbnailUri) {
        this.title = title;
        this.description = description;
        this.videoUri = videoUri;
        this.thumbnailUri = thumbnailUri;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @JsonProperty("video_uri")
    public URI getVideoUri() {
        return videoUri;
    }

    @JsonProperty("thumbnail_uri")
    public URI getThumbnailUri() {
        return thumbnailUri;
    }
}
