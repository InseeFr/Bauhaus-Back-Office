package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.model.ValidationStatus;
import jakarta.validation.constraints.NotNull;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static fr.insee.rmes.config.PropertiesKeys.BASE_URI_GESTION;
import static fr.insee.rmes.config.PropertiesKeys.BASE_URI_PUBLICATION;
import static java.util.Objects.requireNonNull;

@Service
public record PublicationUtils(String baseUriGestion, String baseUriPublication) {
    public PublicationUtils(@Value("${" + BASE_URI_GESTION + "}") String baseUriGestion,
                            @Value("${" + BASE_URI_PUBLICATION + "}") String baseUriPublication) {
        this.baseUriGestion = baseUriGestion;
        this.baseUriPublication = baseUriPublication;
    }

    public Resource tranformBaseURIToPublish(Resource resource) {
        if (!resource.toString().contains(this.baseUriGestion)) return resource;
        String newResource = resource.toString().replace(this.baseUriGestion, this.baseUriPublication);
        return RdfUtils.toURI(newResource);
    }

    public static boolean stringEndsWithItemFromList(@NotNull String inputStr, @NotNull String[] items) {
        return Arrays.stream(items).parallel().anyMatch(requireNonNull(inputStr)::endsWith);
    }

    public static boolean isPublished(String status) {
        return ValidationStatus.UNPUBLISHED.getValue().equals(status) || Constants.UNDEFINED.equals(status);
    }


}
