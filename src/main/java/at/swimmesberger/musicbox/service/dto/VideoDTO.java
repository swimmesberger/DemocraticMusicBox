package at.swimmesberger.musicbox.service.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class VideoDTO {
    private final long playlistID;
    private final String videoURI;

    @JsonCreator
    public VideoDTO(@JsonProperty("playlistID") long playlistID, @JsonProperty("playlistUID") String videoURI) {
        this.playlistID = playlistID;
        this.videoURI = videoURI;
    }

    public long getPlaylistID() {
        return playlistID;
    }

    public String getVideoURI() {
        return videoURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoDTO videoDTO = (VideoDTO) o;
        return Objects.equals(getPlaylistID(), videoDTO.getPlaylistID()) &&
            Objects.equals(getVideoURI(), videoDTO.getVideoURI());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlaylistID(), getVideoURI());
    }
}
