package at.swimmesberger.musicbox.service;

import at.swimmesberger.musicbox.domain.Playlist;
import at.swimmesberger.musicbox.repository.PlaylistRepository;
import at.swimmesberger.musicbox.repository.VideoRepository;
import at.swimmesberger.musicbox.service.dto.VideoDTO;
import at.swimmesberger.musicbox.service.dto.VideoPlatform;
import at.swimmesberger.musicbox.service.errors.InvalidVideoURLException;
import at.swimmesberger.musicbox.service.errors.PlaylistNotFoundException;
import at.swimmesberger.musicbox.service.errors.UnsupportedVideoPlatformException;
import at.swimmesberger.musicbox.service.errors.VideoServiceException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final PlaylistRepository playlistRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VideoService(VideoRepository videoRepository, PlaylistRepository playlistRepository, ApplicationEventPublisher eventPublisher) {
        this.videoRepository = videoRepository;
        this.playlistRepository = playlistRepository;
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    public void addVideoToPlayList(VideoDTO videoDTO) throws VideoServiceException {
        final URI uri;
        try {
            uri = new URI(videoDTO.getVideoURI());
        } catch (URISyntaxException ex) {
            throw new InvalidVideoURLException(ex.getMessage());
        }
        final VideoPlatform platform = this.determineVieoPlatform(uri);
        final Playlist playlist = this.playlistRepository.findById(videoDTO.getPlaylistID()).orElseThrow(() -> new PlaylistNotFoundException("Playlist with id '" + videoDTO.getPlaylistID() + "' not found."));

    }

    private VideoPlatform determineVieoPlatform(URI uri) throws UnsupportedVideoPlatformException {
        if (uri.getHost().toLowerCase().equals("youtube.com")) {
            return VideoPlatform.YOUTUBE;
        } else {
            throw new UnsupportedVideoPlatformException("No platform for host '" + uri.getHost() + "' found.");
        }
    }

}
