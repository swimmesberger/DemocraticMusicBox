package at.swimmesberger.musicbox.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "dmb_vote_video")
public class VideoUserVote implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "playlist_video_entry_id", referencedColumnName = "id")
    private VideoPlaylistEntry playlist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "vote_count", nullable = false)
    private Integer voteCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VideoPlaylistEntry getPlaylist() {
        return playlist;
    }

    public void setPlaylist(VideoPlaylistEntry playlist) {
        this.playlist = playlist;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }
}
