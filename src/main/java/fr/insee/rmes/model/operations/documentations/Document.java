package fr.insee.rmes.model.operations.documentations;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.bauhaus_services.Constants;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.nio.file.Path;

public record Document(String labelLg1,
                       String labelLg2,
                       String descriptionLg1,
                       String descriptionLg2,
                       @JsonProperty(Constants.UPDATED_DATE)
                       String dateMiseAJour,
                       @JsonProperty("lang")
                       String langue,
                       String url,
                       String uri,
                       // TODO remove this attribute
                       String id) {


    public String getId() {
        String idFromUri = StringUtils.substringAfter(uri, "/");
        if (idFromUri != null) {
            return idFromUri;
        }
        return id;
    }

    public Document withUrl(String url) {
        return new Document(this.labelLg1, this.labelLg2, this.descriptionLg1, this.descriptionLg2, this.dateMiseAJour, this.langue, url, this.uri, null);
    }

    public String documentFileName() {
        return path().getFileName().toString();
    }

    public Path path() {
        URI uriFromUrl = URI.create(this.url);
        return uriFromUrl.getScheme()==null?Path.of(this.url):Path.of(uriFromUrl);
    }


}