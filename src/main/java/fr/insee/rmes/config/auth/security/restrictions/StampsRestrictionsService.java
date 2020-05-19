package fr.insee.rmes.config.auth.security.restrictions;

import java.util.List;

import org.openrdf.model.URI;

import fr.insee.rmes.exceptions.RmesException;

public interface StampsRestrictionsService {
	
	boolean isConceptOrCollectionOwner(URI uri) throws RmesException;

	boolean isConceptsOrCollectionsOwner(List<URI> uris) throws RmesException;

	boolean canModifyIndicator(List<URI> uris) throws RmesException;
	
	boolean canValidateIndicator(List<URI> uris) throws RmesException;

	boolean canModifySims(URI targetUri) throws RmesException;

	boolean canCreateOperation(URI seriesURI) throws RmesException;
	
	boolean canCreateSims(List<URI> uris) throws RmesException;
	
	boolean canModifySeries(List<URI> uris) throws RmesException;

	boolean canValidateSeries(List<URI> uris) throws RmesException;

	boolean canCreateConcept() throws RmesException;

	boolean canModifyConcept(URI uri) throws RmesException;

	boolean canCreateFamily() throws RmesException;

	boolean canCreateSeries() throws RmesException;

	boolean canCreateIndicator() throws RmesException;

	boolean canCreateSims(URI targetURI) throws RmesException;

	boolean canModifySeries(URI uri) throws RmesException;

	boolean canValidateSeries(URI uri) throws RmesException;

	boolean canModifyOperation(URI seriesURI) throws RmesException;

	boolean canValidateOperation(URI seriesURI) throws RmesException;

	boolean canModifyIndicator(URI uri) throws RmesException;

	boolean canValidateIndicator(URI uri) throws RmesException;

	boolean canManageDocumentsAndLinks() throws RmesException;
}
