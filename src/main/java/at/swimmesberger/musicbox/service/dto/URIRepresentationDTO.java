package at.swimmesberger.musicbox.service.dto;

import org.springframework.web.util.UriComponents;


public class URIRepresentationDTO {
    private final UriComponents base;
    private final URIRepresentationType type;

    private URIRepresentationDTO(UriComponents base, URIRepresentationType type) {
        this.base = base;
        this.type = type;
    }

    public UriComponents getBase() {
        return base;
    }

    public URIRepresentationType getType() {
        return type;
    }

    public static URIRepresentationDTO createInternal(){
        return new URIRepresentationDTO(null, URIRepresentationType.INTERNAL);
    }

    public static URIRepresentationDTO createExternal(UriComponents base){
        return new URIRepresentationDTO(base, URIRepresentationType.EXTERNAL);
    }
}
