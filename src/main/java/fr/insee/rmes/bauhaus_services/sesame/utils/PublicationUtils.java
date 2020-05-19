package fr.insee.rmes.bauhaus_services.sesame.utils;

import java.util.Arrays;

import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;

public abstract class PublicationUtils {
	
	@Autowired
	static RepositoryUtils repoUtils;
	
	@Autowired
	protected static RepositoryGestion repoGestion;

	private PublicationUtils() {
		throw new IllegalStateException("Utility class");
	}

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
	
	public static RepositoryConnection getRepositoryConnectionGestion() throws RmesException {
		return repoUtils.getConnection(RepositoryGestion.REPOSITORY_GESTION);
	}
}
