package fr.insee.rmes.config.auth.security.restrictions;

import java.util.List;

import org.openrdf.model.URI;

public interface StampsRestrictionsService {
	
	Boolean isConceptOrCollectionOwner(URI URI) throws Exception;

	Boolean isConceptsOrCollectionsOwner(List<URI> URIs) throws Exception;

}
