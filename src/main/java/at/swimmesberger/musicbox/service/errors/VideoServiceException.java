package at.swimmesberger.musicbox.service.errors;

public class VideoServiceException extends Exception {
    public VideoServiceException(String message){
        super(message);
    }

    public VideoServiceException(Throwable cause){
        super(cause);
    }
}
