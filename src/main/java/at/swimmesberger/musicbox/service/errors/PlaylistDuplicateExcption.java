package at.swimmesberger.musicbox.service.errors;

public class PlaylistDuplicateExcption extends VideoServiceException {
    public PlaylistDuplicateExcption(String message) {
        super(message);
    }
}
