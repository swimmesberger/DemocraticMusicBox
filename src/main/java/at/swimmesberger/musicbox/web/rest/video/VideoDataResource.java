package at.swimmesberger.musicbox.web.rest.video;

import at.swimmesberger.musicbox.service.VideoService;
import at.swimmesberger.musicbox.service.dto.URIRepresentationDTO;
import at.swimmesberger.musicbox.service.dto.VideoIdDTO;
import at.swimmesberger.musicbox.service.dto.VideoReturnDTO;
import at.swimmesberger.musicbox.service.errors.UnsupportedVideoPlatformException;
import at.swimmesberger.musicbox.service.errors.VideoServiceException;
import com.codahale.metrics.annotation.Timed;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * This controller serves the static resources for a video
 */
@RestController
@RequestMapping("/api")
public class VideoDataResource {
    public static final int DEFAULT_DAYS_TO_LIVE = 1461; // 4 years
    public static final long DEFAULT_SECONDS_TO_LIVE = TimeUnit.DAYS.toMillis(DEFAULT_DAYS_TO_LIVE);

    // We consider the last modified date is the start up time of the server
    public final static long LAST_MODIFIED = System.currentTimeMillis();

    private long cacheTimeToLive = DEFAULT_SECONDS_TO_LIVE;


    private final VideoService videoService;

    public VideoDataResource(VideoService videoService) {
        this.videoService = videoService;
    }


    @GetMapping("/resources/video/{videoId}")
    @Timed
    public ResponseEntity<Resource> getVideoResource(@PathVariable(value = "videoId") String videoId) throws VideoServiceException {
        final VideoReturnDTO videoByID = this.getVideoById(videoId);
        if (videoByID == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        final URI uri = videoByID.getMetadata().getVideoUri();
        final Path videoPath = Paths.get(uri);
        final PathResource videoResource = new PathResource(videoPath);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
            .header("Content-Disposition", "inline; filename=" + videoByID.getMetadata().getTitle())
            .contentType(MediaTypeFactory
                .getMediaType(videoResource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM))
            .body(videoResource);
    }

    @GetMapping("/resources/thumbnail/{videoId}")
    @Timed
    public ResponseEntity<Resource> getThumbnailResource(@PathVariable(value = "videoId") String videoId, WebRequest request) throws VideoServiceException {
        final VideoReturnDTO videoByID = this.getVideoById(videoId);
        if (videoByID == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(this.isNotModified(request, videoByID)){
            //isNotModified of the WebRequest transparently sets all required parameters;
            return null;
        }
        final URI uri = videoByID.getMetadata().getThumbnailUri();
        final Path thumbnailPath = Paths.get(uri);
        final PathResource thumbnailResource = new PathResource(thumbnailPath);
        return ResponseEntity.status(HttpStatus.OK).headers(this.crateCacheHeaders(videoByID))
            .contentType(MediaTypeFactory
                .getMediaType(thumbnailResource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM))
            .body(thumbnailResource);
    }

    private VideoReturnDTO getVideoById(String videoId) throws UnsupportedVideoPlatformException {
        final VideoIdDTO videoIdDTO = this.videoService.createVideoIdFromIdString(videoId);
        final VideoReturnDTO videoByID = this.videoService.getReturnVideoByID(videoIdDTO, URIRepresentationDTO.createInternal());
        if (videoByID.getMetadata() == null) {
            return null;
        }
        return videoByID;
    }

    private boolean isNotModified(WebRequest request, VideoReturnDTO dto){
        final long lastModified = dto.getProcessingTime();
        final String etag = dto.getIdString();
        return request.checkNotModified(etag, lastModified);
    }

    private HttpHeaders crateCacheHeaders(VideoReturnDTO dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(cacheTimeToLive, TimeUnit.MILLISECONDS));
        headers.setPragma("cache");
        headers.setExpires(cacheTimeToLive + System.currentTimeMillis());
        headers.setLastModified(dto.getProcessingTime());
        headers.setETag("\"" + dto.getIdString() + "\"");
        return headers;
    }
}
