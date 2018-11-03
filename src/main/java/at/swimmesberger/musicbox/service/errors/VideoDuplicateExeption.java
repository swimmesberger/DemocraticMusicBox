package at.swimmesberger.musicbox.service.errors;

public class VideoDuplicateExeption extends VideoServiceException {
    public VideoDuplicateExeption(String message) {
        super(message);
    }
}
