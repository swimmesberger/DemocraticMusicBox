package at.swimmesberger.musicbox.web.rest.video;

import at.swimmesberger.musicbox.domain.Playlist;
import at.swimmesberger.musicbox.service.VideoService;
import at.swimmesberger.musicbox.service.dto.*;
import at.swimmesberger.musicbox.service.errors.PlaylistDuplicateExcption;
import at.swimmesberger.musicbox.service.errors.VideoDuplicateExeption;
import at.swimmesberger.musicbox.service.errors.VideoServiceException;
import at.swimmesberger.musicbox.web.rest.util.PaginationUtil;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PlaylistResource {
    private final Logger logger = LoggerFactory.getLogger(PlaylistResource.class);
    private final VideoService videoService;

    public PlaylistResource(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/playlists")
    @Timed
    public ResponseEntity<List<PlaylistDTO>> getAllPlaylists(UriComponentsBuilder requestUri) {
        final List<PlaylistDTO> playlists = this.videoService.getAllPlaylists(URIRepresentationDTO.createExternal(requestUri));
        return new ResponseEntity<>(playlists, HttpStatus.OK);
    }

    @PostMapping("/playlists")
    @Timed
    public ResponseEntity<PlaylistDTO> createPlaylist(@RequestBody CreatePlaylistDTO playlistDTO) throws PlaylistDuplicateExcption {
        final PlaylistDTO playlist;
        try {
            playlist = this.videoService.createPlaylist(playlistDTO);
        } catch (DataIntegrityViolationException dae) {
            logger.error(dae.getMessage(), dae);
            throw new PlaylistDuplicateExcption("A playlist with the name '" + playlistDTO.getName() + "' exists already.");
        }
        return new ResponseEntity<>(playlist, HttpStatus.CREATED);
    }

    @PostMapping("/playlists/{playlistId}")
    @Timed
    public ResponseEntity<PlaylistDTO> addVideoToPlaylist(@PathVariable(value = "playlistId") long playlistId, @RequestBody VideoToPlaylistDTO vToPlaylist, UriComponentsBuilder requestUri) throws VideoServiceException {
        final VideoToPlaylistDTO videoToPlaylistDTO = new VideoToPlaylistDTO(playlistId, vToPlaylist.getVideoURI());
        try {
            return new ResponseEntity<>(this.videoService.addVideoToPlayList(videoToPlaylistDTO, URIRepresentationDTO.createExternal(requestUri)), HttpStatus.OK);
        }catch(DataIntegrityViolationException ex){
            logger.error(ex.getMessage(), ex);
            throw new VideoDuplicateExeption("This playlist '" + playlistId + "' contains a video with the url '" + videoToPlaylistDTO.getVideoURI() + "' already.");
        }
    }
}
