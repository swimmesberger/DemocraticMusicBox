package at.swimmesberger.musicbox.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDTO {
    private final long id;
    private final String name;
    private final String description;
    private final List<VideoPlaylistEntryDTO> videos;

    @JsonCreator
    public PlaylistDTO(@JsonProperty("id") long id, @JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("videos") List<VideoPlaylistEntryDTO> videos) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.videos = videos;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<VideoPlaylistEntryDTO> getVideos() {
        return videos;
    }

    public PlaylistDTO addVideo(VideoPlaylistEntryDTO video){
        final List<VideoPlaylistEntryDTO> videos = new ArrayList<>(this.videos);
        videos.add(video);
        return new PlaylistDTO(this.getId(), this.getName(), this.getDescription(), videos);
    }
}
