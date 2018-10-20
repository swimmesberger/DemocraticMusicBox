package at.swimmesberger.musicbox.service.errors;

public class UnsupportedVideoPlatformException extends VideoServiceException {
    public UnsupportedVideoPlatformException(String message){
        super(message);
    }
}
