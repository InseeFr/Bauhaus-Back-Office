package fr.insee.rmes.modules.commons.configuration.swagger.model;

import org.springframework.http.MediaType;

public enum Accept {

    JSON(MediaType.APPLICATION_JSON_VALUE),
    XML(MediaType.APPLICATION_XML_VALUE);

    private final String mediaType;

    Accept(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public static Accept fromMediaType(String mediaType) {
        for (Accept accept : values()) {
            if (accept.getMediaType().equalsIgnoreCase(mediaType)) {
                return accept;
            }
        }
        throw new IllegalArgumentException("Unsupported media type: " + mediaType);
    }
}
