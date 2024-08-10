package fr.insee.rmes.bauhaus_services.stamps;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.accesscontrol.StampsRestrictionsVerifier;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.auth.UserProvider;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;


@Service
@Primary
@Deprecated
/*
All authorizations checks including those over data (stamps, ...) must be processed in @PreAuthorize in controllers
so this class shall not be used and therefore should be removed
 */
public class StampsRestrictionServiceImpl implements StampsRestrictionsService {
	
	protected final RepositoryGestion repoGestion;
	private final AuthorizeMethodDecider authorizeMethodDecider;
    private final UserProvider userProvider;
    private final StampsRestrictionsVerifier stampsRestrictionsVerifier;

	private static final Logger logger = LoggerFactory.getLogger(StampsRestrictionServiceImpl.class);

	@Autowired
	public StampsRestrictionServiceImpl(RepositoryGestion repoGestion, AuthorizeMethodDecider authorizeMethodDecider, UserProvider userProvider, StampsRestrictionsVerifier stampsRestrictionsVerifier) {
		this.repoGestion = repoGestion;
		this.authorizeMethodDecider = authorizeMethodDecider;
        this.userProvider = userProvider;
        this.stampsRestrictionsVerifier = stampsRestrictionsVerifier;
    }

	@Override
	public boolean isConceptOrCollectionOwner(IRI uri) throws RmesException {
		return isConceptsOrCollectionsOwner(List.of(uri));
	}

	@Override
	public boolean isConceptsOrCollectionsOwner(List<IRI> uris) throws RmesException {
		if (authorizeMethodDecider.isAdmin()) {
			return true;
		}
		return isConceptOwner(uris);
	}

	private boolean isConceptOwner(List<IRI> uris) throws RmesException {
		return isOwnerForModule(getStampAsString(), uris, ConceptsQueries::getOwner, Constants.OWNER);
	}

	public boolean isSeriesManagerWithStamp(IRI iri, String stamp) throws RmesException {
		return stampsRestrictionsVerifier.isManagerForModule(stamp, iri, OpSeriesQueries::getCreatorsBySeriesUri, Constants.CREATORS);
	}

	protected boolean isSeriesManager(IRI iri) throws RmesException {
		return isSeriesManagerWithStamp(iri, getStampAsString());
	}

	private boolean isIndicatorCreator(IRI iri) throws RmesException {
		return isOwnerForModule(getStampAsString(), List.of(iri), IndicatorsQueries::getCreatorsByIndicatorUri, Constants.CREATORS);
	}
	private boolean isConceptManager(IRI uri) throws RmesException {
		return stampsRestrictionsVerifier.isManagerForModule(getStampAsString(), uri, ConceptsQueries::getManager, Constants.MANAGER);
	}



    private boolean isOwnerForModule(String stamp, List<IRI> uris, QueryGenerator queryGenerator, String stampKey) throws RmesException {
		logger.trace("Check ownership for {} with stamp {}",uris, stampKey);
		return stampsRestrictionsVerifier.checkResponsabilityForModule(stamp, uris, queryGenerator, stampKey, Stream::allMatch);
	}


	private String getStampAsString() {
		return getUser().getStampAsString();
	}


	private boolean canModifyOrValidateIndicator(IRI iri) throws RmesException {
		return (authorizeMethodDecider.isAdmin() || (isIndicatorCreator(iri) && authorizeMethodDecider.isIndicatorContributor()));
	}

	@Override
	public boolean canModifyIndicator(IRI uri) throws RmesException {
		return canModifyOrValidateIndicator(uri);
	}

	@Override
	public boolean canValidateIndicator(IRI uri) throws RmesException {
		return canModifyOrValidateIndicator(uri);
	}

	@Override
	public boolean canModifySims(IRI seriesOrIndicatorUri) throws RmesException {
		return (authorizeMethodDecider.isAdmin() || authorizeMethodDecider.isCnis() || (isSeriesManagerWithStamp(seriesOrIndicatorUri, getStampAsString()) && authorizeMethodDecider.isSeriesContributor())
				|| (isIndicatorCreator(seriesOrIndicatorUri) && authorizeMethodDecider.isIndicatorContributor()));
	}

	@Override
	public boolean canCreateConcept() {
		return (authorizeMethodDecider.isAdmin() || authorizeMethodDecider.isConceptsContributor());
	}

	@Override
	public boolean canCreateFamily() {
		return canCreateFamilySeriesOrIndicator();
	}

	private boolean canCreateFamilySeriesOrIndicator() {
		return authorizeMethodDecider.isAdmin();
	}

	@Override
	public boolean canCreateSeries() {
		return canCreateFamilySeriesOrIndicator();
	}

	@Override
	public boolean canCreateIndicator() {
		return canCreateFamilySeriesOrIndicator();
	}

	@Override
	public boolean canCreateOperation(IRI seriesURI) throws RmesException {
		return (authorizeMethodDecider.isAdmin() || (isSeriesManagerWithStamp(seriesURI, getStampAsString()) && authorizeMethodDecider.isSeriesContributor()));
	}

	@Override
	public boolean canCreateSims(IRI seriesOrIndicatorUri) throws RmesException {
		return (authorizeMethodDecider.isAdmin() || (isSeriesManagerWithStamp(seriesOrIndicatorUri, getStampAsString()) && authorizeMethodDecider.isSeriesContributor())
				|| (isIndicatorCreator(seriesOrIndicatorUri) && authorizeMethodDecider.isIndicatorContributor()));
	}

	@Override
	public boolean canDeleteSims() {
		return authorizeMethodDecider.isAdmin();
	}
	
	@Override
	public boolean canModifyConcept(IRI uri) throws RmesException {
		return authorizeMethodDecider.isAdmin() || authorizeMethodDecider.isConceptsContributor() || (isConceptManager(uri) && authorizeMethodDecider.isConceptContributor())
				|| (isConceptOwner(List.of(uri)) && authorizeMethodDecider.isConceptCreator());
	}

	@Override
	public boolean canModifySeries(IRI uri) throws RmesException {
		return ((isSeriesManagerWithStamp(uri, getStampAsString()) && authorizeMethodDecider.isSeriesContributor()) || authorizeMethodDecider.isAdmin() || authorizeMethodDecider.isCnis() );
	}

	@Override
	public boolean canModifyOperation(IRI seriesURI) throws RmesException {
		return canModifySeries(seriesURI);
	}

	@Override
	public boolean canValidateSeries(IRI uri) throws RmesException {
		return (authorizeMethodDecider.isAdmin() || (isSeriesManagerWithStamp(uri, getStampAsString()) && authorizeMethodDecider.isSeriesContributor()));
	}

	@Override
	public boolean canManageDocumentsAndLinks() {
		return (authorizeMethodDecider.isAdmin() || authorizeMethodDecider.isSeriesContributor() || authorizeMethodDecider.isIndicatorContributor());
	}

	@Override
	public boolean canValidateClassification(IRI uri) {
		return authorizeMethodDecider.isAdmin();
	}

	private User getUser() {
		return this.userProvider.findUserDefaultToEmpty();
	}

	@Override
	public boolean isAdmin() {
		return authorizeMethodDecider.isAdmin();
	}

}
