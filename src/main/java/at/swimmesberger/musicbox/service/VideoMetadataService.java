package at.swimmesberger.musicbox.service;

import at.swimmesberger.musicbox.domain.Video;
import at.swimmesberger.musicbox.domain.VideoId;
import at.swimmesberger.musicbox.repository.VideoRepository;
import at.swimmesberger.musicbox.service.dto.VideoPlatform;
import at.swimmesberger.musicbox.service.errors.UnsupportedVideoPlatformException;
import at.swimmesberger.musicbox.service.errors.VideoMetadataServiceException;
import at.swimmesberger.musicbox.service.errors.VideoServiceException;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Service
public class VideoMetadataService {
    private final VideoRepository videoRepository;

    public VideoMetadataService(VideoRepository videoRepository){
        this.videoRepository =  videoRepository;
    }

    public Video getVideo(URI uri) throws VideoServiceException  {
        final VideoId videoId = this.createVideoId(uri);
        final Optional<Video> videoOp = this.videoRepository.findById(videoId);
        if(videoOp.isPresent()){
            return videoOp.get();
        }
        return this.scrapeVideo(videoId);
    }

    private Video scrapeVideo(VideoId videoId) throws VideoServiceException {
        final URI uri = this.buildURI(videoId);
        try {
            final Document doc = HttpConnection.connect(uri.toURL()).get();
            //TODO: implement
            return null;
        }catch(IOException ex){
            throw new VideoMetadataServiceException("Can't retrieve youtube video page.");
        }
    }

    private URI buildURI(VideoId videoId)throws UnsupportedVideoPlatformException{
        if(videoId.getVideoPlatform() == VideoPlatform.YOUTUBE){
            return UriComponentsBuilder.newInstance().scheme("https").host("youtube.com").queryParam("v", videoId.getVideoId()).build().toUri();
        }else{
            throw newUnsupprotedVideoPlatform(videoId.getVideoPlatform());
        }
    }

    private VideoId createVideoId(URI uri) throws UnsupportedVideoPlatformException {
        final UriComponents uriComponents = UriComponentsBuilder.fromUri(uri).build();
        final VideoPlatform platform = this.determineVieoPlatform(uriComponents);
        final String videoId;
        if(platform == VideoPlatform.YOUTUBE){
            MultiValueMap<String, String> parameters = uriComponents.getQueryParams();
            videoId = parameters.getFirst("v");
        }else {
            throw newUnsupprotedVideoPlatform(platform);
        }
        return new VideoId(videoId, platform);
    }


    private VideoPlatform determineVieoPlatform(UriComponents uri) throws UnsupportedVideoPlatformException {
        if (uri.getHost().toLowerCase().equals("youtube.com")) {
            return VideoPlatform.YOUTUBE;
        } else {
            throw new UnsupportedVideoPlatformException("No platform for host '" + uri.getHost() + "' found.");
        }
    }

    private UnsupportedVideoPlatformException newUnsupprotedVideoPlatform(VideoPlatform platform){
        return new UnsupportedVideoPlatformException("Patform '" + platform + "' is currently not supported.");
    }
}
