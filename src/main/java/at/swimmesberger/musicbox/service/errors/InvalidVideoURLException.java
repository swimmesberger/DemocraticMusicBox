package at.swimmesberger.musicbox.service.errors;

public class InvalidVideoURLException extends VideoServiceException {
    public InvalidVideoURLException(String message){
        super(message);
    }
}
