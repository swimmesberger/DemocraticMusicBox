package at.swimmesberger.musicbox.service.processing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoMetadata {
    private final String id;
    private final String title;
    private final String description;
    private final int duration;
    private final int width;
    private final int height;
    private final List<String> tags;
    private final String uploadDate;
    //file extension
    private final String ext;

    public VideoMetadata(@JsonProperty("id") String id, @JsonProperty("title") String title, @JsonProperty("description") String description, @JsonProperty("duration") int duration,
                         @JsonProperty("width") int width, @JsonProperty("height") int height, @JsonProperty("tags") List<String> tags, @JsonProperty("upload_date") String uploadDate, @JsonProperty("ext") String ext) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.width = width;
        this.height = height;
        this.tags = tags;
        this.uploadDate = uploadDate;
        this.ext = ext;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getDuration() {
        return duration;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<String> getTags() {
        return tags;
    }

    @JsonProperty("upload_date")
    public String getUploadDate() {
        return uploadDate;
    }

    public String getExt() {
        return ext;
    }

    @Override
    public String toString() {
        return "VideoMetadata{" +
            "id='" + id + '\'' +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", duration=" + duration +
            ", width=" + width +
            ", height=" + height +
            ", tags=" + tags +
            ", uploadDate='" + uploadDate + '\'' +
            ", ext='" + ext + '\'' +
            '}';
    }
}
