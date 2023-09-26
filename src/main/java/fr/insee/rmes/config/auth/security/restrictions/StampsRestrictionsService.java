package fr.insee.rmes.config.auth.security.restrictions;

import fr.insee.rmes.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;

import java.util.List;

public interface StampsRestrictionsService {
	
	/*
	 * USER
	 */
	
	/*
	 * CONCEPTS AND COLLECTIONS
	 */
		
	boolean isConceptOrCollectionOwner(IRI uri) throws RmesException;

	boolean isConceptsOrCollectionsOwner(List<IRI> uris) throws RmesException;

	boolean canCreateConcept() throws RmesException;

	boolean canModifyConcept(IRI uri) throws RmesException;

	boolean isAdmin();


	/*
	 * INDICATORS
	 */
	boolean canCreateIndicator() throws RmesException;

	boolean canModifyIndicator(IRI uri) throws RmesException;

	boolean canValidateIndicator(IRI uri) throws RmesException;

	
	/*
	 * DOCUMENTATION SIMS (OPERATION MODULE)
	 */
	boolean canCreateSims(IRI seriesOrIndicatorUri) throws RmesException;

	boolean canModifySims(IRI seriesOrIndicatorUri) throws RmesException;

	boolean canDeleteSims() throws RmesException;

	
	/*
	 * OPERATIONS
	 */
	boolean canCreateOperation(IRI seriesURI) throws RmesException;
	
	boolean canModifyOperation(IRI seriesURI) throws RmesException;
	
	boolean canValidateOperation(IRI seriesURI) throws RmesException;


	
	/*
	 * SERIES
	 */
	boolean canCreateSeries() throws RmesException;

	boolean canModifySeries(IRI uri) throws RmesException;

	boolean canValidateSeries(IRI uri) throws RmesException;

	boolean isSeriesManager(IRI uri) throws RmesException;

	/*
	 * FAMILIES (OPERATION MODULE)
	 */
	boolean canCreateFamily() throws RmesException;


	/*
	 * DOCUMENTS
	 */
	boolean canManageDocumentsAndLinks() throws RmesException;
	
	/*
	 * CLASSIFICATIONS
	 */
	boolean canValidateClassification(IRI uri) throws RmesException;


}
