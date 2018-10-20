package at.swimmesberger.musicbox.service.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class VideoDTO {
    private final String playlistUID;
    private final String videoURI;

    @JsonCreator
    public VideoDTO(@JsonProperty("playlistUID") String playlistUID, @JsonProperty("playlistUID") String videoURI) {
        this.playlistUID = playlistUID;
        this.videoURI = videoURI;
    }

    public String getPlaylistUID() {
        return playlistUID;
    }

    public String getVideoURI() {
        return videoURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoDTO videoDTO = (VideoDTO) o;
        return Objects.equals(getPlaylistUID(), videoDTO.getPlaylistUID()) &&
            Objects.equals(getVideoURI(), videoDTO.getVideoURI());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlaylistUID(), getVideoURI());
    }
}
