package at.swimmesberger.musicbox.domain;

import at.swimmesberger.musicbox.service.dto.VideoIdDTO;
import at.swimmesberger.musicbox.service.dto.VideoPlatform;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "dmb_video")
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private VideoId id;

    @Size(max = 255)
    @Column(length = 255)
    private String title;

    @Lob
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return Objects.equals(getId(), video.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public static List<VideoIdDTO> toVideoIdDTO(Collection<Video> videos){
        final List<VideoIdDTO> dtos = new ArrayList<>(videos.size());
        for(final Video video : videos){
            dtos.add(video.getId().toDTO());
        }
        return dtos;
    }
}
