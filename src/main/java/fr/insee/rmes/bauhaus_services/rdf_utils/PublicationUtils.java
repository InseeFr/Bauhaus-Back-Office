package fr.insee.rmes.bauhaus_services.rdf_utils;

import java.util.Arrays;

import org.eclipse.rdf4j.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.model.ValidationStatus;

@Service
public abstract class PublicationUtils {
	
	@Autowired
	private static Config config;
	
	private PublicationUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static Resource tranformBaseURIToPublish(Resource resource) {
		if (!resource.toString().contains(config.getBaseUriGestion())) return resource;
		String newResource = resource.toString().replace(config.getBaseUriGestion(), config.getBaseUriPublication());
		return RdfUtils.toURI(newResource);
	}
	
	public static boolean stringEndsWithItemFromList(String inputStr, String[] items) {
		return Arrays.stream(items).parallel().anyMatch(inputStr::endsWith);
	}

	public static boolean isPublished(String status) {
		return status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED);
	}
	
	public static void setConfig(Config config) {
		PublicationUtils.config = config;
	}
	
	
}
