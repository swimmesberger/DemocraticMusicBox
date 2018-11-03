package at.swimmesberger.musicbox.web.rest;

import at.swimmesberger.musicbox.domain.Video;
import at.swimmesberger.musicbox.service.VideoService;
import at.swimmesberger.musicbox.web.rest.util.PaginationUtil;
import com.codahale.metrics.annotation.Timed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<Video>> getAllVideos(Pageable pageable) {
        final Page<Video> page = this.videoService.getAllVideos(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/videos");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
