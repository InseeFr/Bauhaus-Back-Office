package fr.insee.rmes.config.auth.security.restrictions;

import java.util.List;

import org.openrdf.model.URI;

import fr.insee.rmes.exceptions.RmesException;

public interface StampsRestrictionsService {
	
	Boolean isConceptOrCollectionOwner(URI uri) throws RmesException;

	Boolean isConceptsOrCollectionsOwner(List<URI> uris) throws RmesException;

}
