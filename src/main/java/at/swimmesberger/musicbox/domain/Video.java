package at.swimmesberger.musicbox.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "dmb_video")
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private VideoId id;
    private VideoPlatform platform;
    private String title;
    private String description;
    private String thumbnailURI;
    private String videoURI;

    public VideoId getId() {
        return id;
    }

    public void setId(VideoId id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailURI() {
        return thumbnailURI;
    }

    public void setThumbnailURI(String thumbnailURI) {
        this.thumbnailURI = thumbnailURI;
    }

    public String getVideoURI() {
        return videoURI;
    }

    public void setVideoURI(String videoURI) {
        this.videoURI = videoURI;
    }

    public VideoPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(VideoPlatform platform) {
        this.platform = platform;
    }
}