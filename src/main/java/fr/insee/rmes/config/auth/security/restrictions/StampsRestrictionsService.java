package fr.insee.rmes.config.auth.security.restrictions;

import java.util.List;

import org.openrdf.model.URI;

import fr.insee.rmes.exceptions.RmesException;

public interface StampsRestrictionsService {
	
	Boolean isConceptOrCollectionOwner(URI uri) throws RmesException;

	Boolean isConceptsOrCollectionsOwner(List<URI> uris) throws RmesException;

	Boolean canModifyIndicator(List<URI> uris) throws RmesException;
	
	Boolean canValidateIndicator(List<URI> uris) throws RmesException;

	Boolean canModifySims(URI targetUri) throws RmesException;

	Boolean canCreateOperation(URI seriesURI) throws RmesException;
	
	Boolean canCreateSims(List<URI> uris) throws RmesException;
	
	Boolean canModifySeries(List<URI> uris) throws RmesException;

	Boolean canValidateSeries(List<URI> uris) throws RmesException;

	Boolean canCreateConcept() throws RmesException;

	Boolean canModifyConcept(URI uri) throws RmesException;

	Boolean canCreateFamily() throws RmesException;

	Boolean canCreateSeries() throws RmesException;

	Boolean canCreateIndicator() throws RmesException;

	Boolean canCreateSims(URI targetURI) throws RmesException;
}
