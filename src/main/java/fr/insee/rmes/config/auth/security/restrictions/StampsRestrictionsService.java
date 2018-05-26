package fr.insee.rmes.config.auth.security.restrictions;

import java.util.List;

import org.openrdf.model.URI;

public interface StampsRestrictionsService {

	Boolean isConceptsOrCollectionsOwner(List<URI> conceptsURI) throws Exception;

}
