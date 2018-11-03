package at.swimmesberger.musicbox.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlaylistPostProcessDTO {
    private final long playlistId;

    @JsonCreator
    public PlaylistPostProcessDTO(@JsonProperty("playlist_id") long playlistId) {
        this.playlistId = playlistId;
    }

    @JsonProperty("playlist_id")
    public long getPlaylistId() {
        return playlistId;
    }
}
