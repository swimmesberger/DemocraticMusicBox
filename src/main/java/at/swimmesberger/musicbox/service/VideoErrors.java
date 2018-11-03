package at.swimmesberger.musicbox.service;

import at.swimmesberger.musicbox.service.dto.VideoPlatform;
import at.swimmesberger.musicbox.service.errors.UnsupportedVideoPlatformException;

public class VideoErrors {
    public static UnsupportedVideoPlatformException newUnsupprotedVideoPlatform(VideoPlatform platform){
        return new UnsupportedVideoPlatformException("Patform '" + platform + "' is currently not supported.");
    }
}
