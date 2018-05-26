package fr.insee.rmes.config.auth.security.restrictions;

import java.util.List;

import org.openrdf.model.URI;

public interface StampsRestrictionsService {
	
	Boolean isConceptOwner(String conceptURI) throws Exception;

	Boolean isConceptsOwner(List<URI> conceptsURI) throws Exception;

}
