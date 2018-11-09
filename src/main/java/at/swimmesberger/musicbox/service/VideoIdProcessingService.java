package at.swimmesberger.musicbox.service;

import at.swimmesberger.musicbox.service.dto.VideoIdDTO;
import at.swimmesberger.musicbox.service.dto.VideoPlatform;
import at.swimmesberger.musicbox.service.dto.VideoUnit;
import at.swimmesberger.musicbox.service.errors.InvalidVideoURLException;
import at.swimmesberger.musicbox.service.errors.UnsupportedVideoPlatformException;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class VideoIdProcessingService {
    public VideoUnit createVideoUnit(VideoIdDTO videoId) throws UnsupportedVideoPlatformException {
        return new VideoUnit(videoId, this.createIdStringFromVideoId(videoId), this.constructURI(videoId));
    }

    public URI constructURI(VideoIdDTO videoId) throws UnsupportedVideoPlatformException {
        if (videoId.getVideoPlatform() == VideoPlatform.YOUTUBE) {
            return UriComponentsBuilder.newInstance().scheme("https").host("youtube.com").path("watch").queryParam("v", videoId.getVideoId()).build().toUri();
        } else {
            throw VideoErrors.newUnsupprotedVideoPlatform(videoId.getVideoPlatform());
        }
    }

    public VideoIdDTO createVideoIdFromIdString(String idString) throws UnsupportedVideoPlatformException {
        final String[] split = idString.split("\\_");
        if (split.length != 2) throw new UnsupportedVideoPlatformException("Invalid id given '" + idString + "'");
        final String id = split[0];
        final String platformString = split[1].toUpperCase();
        final VideoPlatform platform = VideoPlatform.valueOf(platformString);
        if (platform == null)
            throw new UnsupportedVideoPlatformException("No platform with name '" + platformString + "' found.");
        return new VideoIdDTO(id, platform);
    }

    public String createIdStringFromVideoId(VideoIdDTO id){
        final StringBuilder sb = new StringBuilder(id.getVideoId());
        sb.append('_');
        sb.append(id.getVideoPlatform().toString().toLowerCase());
        return sb.toString();
    }

    public VideoIdDTO createVideoId(String uriString) throws InvalidVideoURLException, UnsupportedVideoPlatformException {
        final URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException ex) {
            throw new InvalidVideoURLException(ex.getMessage());
        }
        return this.createVideoId(uri);
    }

    public VideoIdDTO createVideoId(URI uri) throws UnsupportedVideoPlatformException {
        final UriComponents uriComponents = UriComponentsBuilder.fromUri(uri).build();
        final VideoPlatform platform = this.determineVieoPlatform(uriComponents);
        final String videoId;
        if (platform == VideoPlatform.YOUTUBE) {
            MultiValueMap<String, String> parameters = uriComponents.getQueryParams();
            videoId = parameters.getFirst("v");
        } else {
            throw VideoErrors.newUnsupprotedVideoPlatform(platform);
        }
        return new VideoIdDTO(videoId, platform);
    }

    private VideoPlatform determineVieoPlatform(UriComponents uri) throws UnsupportedVideoPlatformException {
        final String host = uri.getHost().toLowerCase();
        if (host.equals("youtube.com") || host.equals("www.youtube.com")) {
            return VideoPlatform.YOUTUBE;
        } else {
            throw new UnsupportedVideoPlatformException("No platform for host '" + uri.getHost() + "' found.");
        }
    }
}
