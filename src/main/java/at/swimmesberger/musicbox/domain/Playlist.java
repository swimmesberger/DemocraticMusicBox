package at.swimmesberger.musicbox.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dmb_playlist")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private String name;

    private String description;

    @OneToMany
    @JoinTable
    (
        name = "dmb_paylist_video",
        joinColumns = {@JoinColumn(name = "playlist_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "video_id", referencedColumnName = "id")}
    )
    private List<Video> videos;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }
}
