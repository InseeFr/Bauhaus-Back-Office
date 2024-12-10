package fr.insee.rmes.model.operations.documentations;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
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
                       String uri) {


    public Document(String id) {
        this(id, false);
    }

    public Document(String id, boolean isLink) {
        this(null, null, null, null, null, null, null, uriFromId(id, isLink));
    }

    private static String uriFromId(String id, boolean isLink) {
        return RdfUtils.toString(
                isLink ? RdfUtils.linkIRI(id) :
                        RdfUtils.documentIRI(id)
        );
    }

    public String getId() {
        return StringUtils.substringAfter(uri, "/");
    }

    public Document withUrl(String url) {
        return new Document(this.labelLg1, this.labelLg2, this.descriptionLg1, this.descriptionLg2, this.dateMiseAJour, this.langue, url, this.uri);
    }

    public Document withId(String id, boolean isLink) {
        return new Document(this.labelLg1, this.labelLg2, this.descriptionLg1, this.descriptionLg2, this.dateMiseAJour, this.langue, this.url, uriFromId(id, isLink));
    }

    public String documentFileName() {
        return Path.of(URI.create(this.url)).getFileName().toString();
    }
}