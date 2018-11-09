package at.swimmesberger.musicbox.service.errors;

public class VideoServiceException extends RuntimeException {
    public VideoServiceException(String message){
        super(message);
    }

    public VideoServiceException(Throwable cause){
        super(cause);
    }
}
