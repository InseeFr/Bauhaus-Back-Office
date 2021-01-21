package fr.insee.rmes.config.auth.security.restrictions;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;

public interface StampsRestrictionsService {
	
	User getUser() throws RmesException;
	
	boolean isConceptOrCollectionOwner(IRI uri) throws RmesException;

	boolean isConceptsOrCollectionsOwner(List<IRI> uris) throws RmesException;

	boolean canModifyIndicator(List<IRI> uris) throws RmesException;
	
	boolean canValidateIndicator(List<IRI> uris) throws RmesException;

	boolean canModifySims(IRI targetUri) throws RmesException;

	boolean canCreateOperation(IRI seriesURI) throws RmesException;
	
	boolean canCreateSims(List<IRI> uris) throws RmesException;
	
	boolean canModifySeries(List<IRI> uris) throws RmesException;

	boolean canValidateSeries(List<IRI> uris) throws RmesException;

	boolean isSeriesManager(IRI uri) throws RmesException;

	boolean canCreateConcept() throws RmesException;

	boolean canModifyConcept(IRI uri) throws RmesException;

	boolean canCreateFamily() throws RmesException;

	boolean canCreateSeries() throws RmesException;

	boolean canCreateIndicator() throws RmesException;

	boolean canCreateSims(IRI targetURI) throws RmesException;

	boolean canDeleteSims(IRI seriesURI) throws RmesException;

	boolean canDeleteSims(List<IRI> uris) throws RmesException;
	
	boolean canModifySeries(IRI uri) throws RmesException;

	boolean canValidateSeries(IRI uri) throws RmesException;

	boolean canModifyOperation(IRI seriesURI) throws RmesException;

	boolean canValidateOperation(IRI seriesURI) throws RmesException;

	boolean canModifyIndicator(IRI uri) throws RmesException;

	boolean canValidateIndicator(IRI uri) throws RmesException;

	boolean canManageDocumentsAndLinks() throws RmesException;
}
