package at.swimmesberger.musicbox.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "dmb_playlist")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(unique=true)
    private String name;

    @Lob
    private String description;

    @OneToMany
    @JoinTable
    (
        name = "dmb_paylist_video",
        joinColumns = {@JoinColumn(name = "playlist_id", referencedColumnName = "id")},
        inverseJoinColumns = {
            @JoinColumn(name = "video_id", referencedColumnName = "video_id"),
            @JoinColumn(name = "video_platform", referencedColumnName = "video_platform")
        }
    )
    private List<Video> videos;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
