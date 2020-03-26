package fr.insee.rmes.persistance.service.sesame.utils;

import java.util.Arrays;

import org.openrdf.model.Resource;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.modele.ValidationStatus;
import fr.insee.rmes.persistance.service.Constants;

public class PublicationUtils {

	public static Resource tranformBaseURIToPublish(Resource resource) {
		String newResource = resource.toString().replace(Config.BASE_URI_GESTION, Config.BASE_URI_PUBLICATION);
		return SesameUtils.toURI(newResource);
	}
	
	public static boolean stringEndsWithItemFromList(String inputStr, String[] items) {
	    return Arrays.stream(items).parallel().anyMatch(inputStr::endsWith);
	}
	
	public static boolean isPublished(String status) {
		return status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED);
	}
}
