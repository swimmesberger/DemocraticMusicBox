package at.swimmesberger.musicbox.service.errors;

public class VideoProcessingException extends VideoServiceException {
    public VideoProcessingException(String message) {
        super(message);
    }

    public VideoProcessingException(Throwable cause){
        super(cause);
    }
}
