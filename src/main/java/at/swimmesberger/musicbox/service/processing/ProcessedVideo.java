package at.swimmesberger.musicbox.service.processing;

import at.swimmesberger.musicbox.service.dto.VideoUnit;

import java.net.URI;

public class ProcessedVideo {
    private final VideoUnit source;
    private final URI videoURI;
    private final URI thumbnailURI;
    private final VideoMetadata metadata;

    public ProcessedVideo(VideoUnit source, URI videoURI, URI thumbnailURI, VideoMetadata metadata) {
        this.source = source;
        this.videoURI = videoURI;
        this.thumbnailURI = thumbnailURI;
        this.metadata = metadata;
    }

    public VideoUnit getSource() {
        return source;
    }

    public URI getVideoURI() {
        return videoURI;
    }

    public URI getThumbnailURI() {
        return thumbnailURI;
    }

    public VideoMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "ProcessedVideo{" +
            "source=" + source +
            ", videoURI=" + videoURI +
            ", thumbnailURI=" + thumbnailURI +
            ", metadata=" + metadata +
            '}';
    }
}
