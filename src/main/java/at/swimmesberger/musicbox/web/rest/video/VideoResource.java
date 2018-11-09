package at.swimmesberger.musicbox.web.rest.video;

import at.swimmesberger.musicbox.service.VideoService;
import at.swimmesberger.musicbox.service.dto.URIRepresentationDTO;
import at.swimmesberger.musicbox.service.dto.VideoIdDTO;
import at.swimmesberger.musicbox.service.dto.VideoReturnDTO;
import at.swimmesberger.musicbox.service.errors.VideoServiceException;
import com.codahale.metrics.annotation.Timed;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VideoResource {
    private final VideoService videoService;

    public VideoResource(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/videos")
    @Timed
    public ResponseEntity<List<VideoReturnDTO>> getAllVideos(UriComponentsBuilder requestUri) {
        final List<VideoReturnDTO> videos = this.videoService.getAllVideos(this.createURIRepresentation(requestUri));
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @GetMapping("videos/{videoId}")
    @Timed
    public ResponseEntity<VideoReturnDTO> getVideoByID(@PathVariable(value = "videoId") String videoId, UriComponentsBuilder requestUri) throws VideoServiceException {
        final VideoIdDTO videoIdDTO = this.videoService.createVideoIdFromIdString(videoId);
        final VideoReturnDTO video = this.videoService.getReturnVideoByID(videoIdDTO, this.createURIRepresentation(requestUri));
        return new ResponseEntity<>(video, HttpStatus.OK);
    }

    private URIRepresentationDTO createURIRepresentation(UriComponentsBuilder request){
        return URIRepresentationDTO.createExternal(request);
    }
}
