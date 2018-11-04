package at.swimmesberger.musicbox.service.dto;

import java.net.URI;

public class VideoUnit {
    private final VideoIdDTO id;
    private final URI uri;
    private final String idString;

    public VideoUnit(VideoIdDTO id, String idString, URI uri) {
        this.id = id;
        this.idString = idString;
        this.uri = uri;
    }

    public VideoIdDTO getId() {
        return id;
    }

    public String getIdString() {
        return idString;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "VideoUnit{" +
            "id=" + id +
            ", uri=" + uri +
            ", idString='" + idString + '\'' +
            '}';
    }
}
