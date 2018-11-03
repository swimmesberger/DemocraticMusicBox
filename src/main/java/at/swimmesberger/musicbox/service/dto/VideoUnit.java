package at.swimmesberger.musicbox.service.dto;

import at.swimmesberger.musicbox.domain.VideoId;

import java.net.URI;

public class VideoUnit {
    private final VideoIdDTO id;
    private final URI uri;

    public VideoUnit(VideoIdDTO id, URI uri) {
        this.id = id;
        this.uri = uri;
    }

    public VideoIdDTO getId() {
        return id;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "VideoUnit{" +
            "id=" + id +
            ", uri=" + uri +
            '}';
    }
}
