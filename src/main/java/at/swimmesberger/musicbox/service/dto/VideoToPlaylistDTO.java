package at.swimmesberger.musicbox.service.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class VideoToPlaylistDTO {
    private final long playlistID;
    private final String videoURI;

    @JsonCreator
    public VideoToPlaylistDTO(@JsonProperty("playlist_id") long playlistID, @JsonProperty("video_uri") String videoURI) {
        this.playlistID = playlistID;
        this.videoURI = videoURI;
    }

    @JsonProperty("playlist_id")
    public long getPlaylistID() {
        return playlistID;
    }

    @JsonProperty("video_uri")
    public String getVideoURI() {
        return videoURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoToPlaylistDTO videoToPlaylistDTO = (VideoToPlaylistDTO) o;
        return Objects.equals(getPlaylistID(), videoToPlaylistDTO.getPlaylistID()) &&
            Objects.equals(getVideoURI(), videoToPlaylistDTO.getVideoURI());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlaylistID(), getVideoURI());
    }

    @Override
    public String toString() {
        return "VideoToPlaylistDTO{" +
            "playlistID=" + playlistID +
            ", videoURI=" + videoURI +
            '}';
    }
}
