package at.swimmesberger.musicbox.service.youtube;

import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class YoutubeService {
    private static final String THUMBNAIL = "https://img.youtube.com/vi/6ONRf7h3Mdk/maxresdefault.jpg";


    public URI getDirectVideoURI(YoutubeConfig config){
        final String urlDecodeMapString = config.getArgs().getUrlEncodedFmtStreamMap();
        final MultiValueMap<String, String> urlDecodeMap =  UriComponentsBuilder.newInstance().host("example.com").query(urlDecodeMapString).build().getQueryParams();
        return UriComponentsBuilder.fromUriString(urlDecodeMap.getFirst("url")).build().toUri();
    }
}
