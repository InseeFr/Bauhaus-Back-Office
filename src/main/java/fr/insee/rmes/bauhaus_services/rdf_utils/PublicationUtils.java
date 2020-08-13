package fr.insee.rmes.bauhaus_services.rdf_utils;

import java.util.Arrays;

import org.eclipse.rdf4j.model.Resource;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.model.ValidationStatus;

public abstract class PublicationUtils {

	private PublicationUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static Resource tranformBaseURIToPublish(Resource resource) {
		String newResource = resource.toString().replace(Config.BASE_URI_GESTION, Config.BASE_URI_PUBLICATION);
		return RdfUtils.toURI(newResource);
	}

	public static boolean stringEndsWithItemFromList(String inputStr, String[] items) {
		return Arrays.stream(items).parallel().anyMatch(inputStr::endsWith);
	}

	public static boolean isPublished(String status) {
		return status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED);
	}
	
}
