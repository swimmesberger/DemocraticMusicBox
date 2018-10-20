package at.swimmesberger.musicbox.service;

import at.swimmesberger.musicbox.service.dto.VideoDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class VideoService {
    private final ApplicationEventPublisher eventPublisher;

    public VideoService(ApplicationEventPublisher eventPublisher){
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    public void addVideoToPlayList(VideoDTO videoDTO){
        final URI uri;
        try{

        }catch
    }

    public static class InvalidVideoURLException extends Exception {
        public InvalidVideoURLException(String message){
            super(message);
        }
    }
}
