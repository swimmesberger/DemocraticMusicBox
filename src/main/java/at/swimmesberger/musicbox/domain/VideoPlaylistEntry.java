package at.swimmesberger.musicbox.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "dmb_playlist_video")
public class VideoPlaylistEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "playlist_id", referencedColumnName = "id")
    private Playlist playlist;

    @ManyToOne(optional = false)
    @JoinColumns({
        @JoinColumn(name = "video_id", referencedColumnName = "video_id"),
        @JoinColumn(name = "video_platform", referencedColumnName = "video_platform")
    })
    private Video video;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }
}
