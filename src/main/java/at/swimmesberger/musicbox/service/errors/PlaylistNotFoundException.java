package at.swimmesberger.musicbox.service.errors;

public class PlaylistNotFoundException extends VideoServiceException {
    public PlaylistNotFoundException(String message){
        super(message);
    }
}
