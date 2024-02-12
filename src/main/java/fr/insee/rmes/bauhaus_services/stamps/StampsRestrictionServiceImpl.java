package fr.insee.rmes.bauhaus_services.stamps;

import fr.insee.rmes.bauhaus_services.Constants;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static fr.insee.rmes.utils.StringUtils.urisAsString;


@Service
@Primary
public class StampsRestrictionServiceImpl implements StampsRestrictionsService {
	
	private final RepositoryGestion repoGestion;
	private final AuthorizeMethodDecider authorizeMethodDecider;
	private final UserProvider userProvider;

	private static final Logger logger = LoggerFactory.getLogger(StampsRestrictionServiceImpl.class);

	@Autowired
	public StampsRestrictionServiceImpl(RepositoryGestion repoGestion, AuthorizeMethodDecider authorizeMethodDecider, UserProvider userProvider) {
		this.repoGestion = repoGestion;
		this.authorizeMethodDecider = authorizeMethodDecider;
		this.userProvider = userProvider;
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
		return isOwnerForModule(getStamp(), uris, ConceptsQueries::getOwner, Constants.OWNER);
	}

	private boolean isConceptManager(IRI uri) throws RmesException {
		return isManagerForModule(getStamp(), uri, ConceptsQueries::getManager, Constants.MANAGER);
	}


	public boolean isSeriesManagerWithStamp(IRI iri, String stamp) throws RmesException {
		return isManagerForModule(stamp, iri, OpSeriesQueries::getCreatorsBySeriesUri, Constants.CREATORS);
	}

	protected boolean isSeriesManager(IRI iri) throws RmesException {
		return isSeriesManagerWithStamp(iri, getStamp());
	}

	private boolean isIndicatorCreator(IRI iri) throws RmesException {
		return isOwnerForModule(getStamp(), List.of(iri), IndicatorsQueries::getCreatorsByIndicatorUri, Constants.CREATORS);
	}

	private boolean isManagerForModule(String stamp, IRI uri, QueryGenerator queryGenerator, String stampKey) throws RmesException {
		logger.trace("Check management access for {} with stamp {}",uri, stampKey);
		return checkResponsabilityForModule(stamp, List.of(uri), queryGenerator, stampKey, Stream::anyMatch);
	}

	private boolean isOwnerForModule(String stamp, List<IRI> uris, QueryGenerator queryGenerator, String stampKey) throws RmesException {
		logger.trace("Check ownership for {} with stamp {}",uris, stampKey);
		return checkResponsabilityForModule(stamp, uris, queryGenerator, stampKey, Stream::allMatch);
	}

	private boolean checkResponsabilityForModule(String stamp, List<IRI> uris, QueryGenerator queryGenerator, String stampKey, BiPredicate<Stream<Object>, Predicate<Object>> predicateMatcher) throws RmesException {
		JSONArray owners = repoGestion.getResponseAsArray(queryGenerator.generate(urisAsString(uris)));
		return StringUtils.hasLength(stamp) &&
				predicateMatcher.test(
						owners.toList().stream()
								.map(o -> findStamp(o, stampKey)),
						stamp::equals // apply predicate `stamp::equals` to the stream of stamps returned at the previous line
				);
	}

	protected String getStamp() {
		return getUser().getStamp();
	}

	private Object findStamp(Object o, String stampKey) {
		if (o instanceof JSONObject jsonObject) {
			return jsonObject.get(stampKey);
		}
		if (o instanceof Map<?, ?> map) {
			return map.get(stampKey);
		}
		return null;
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
		return (authorizeMethodDecider.isAdmin() || authorizeMethodDecider.isCnis() || (isSeriesManagerWithStamp(seriesOrIndicatorUri, getStamp()) && authorizeMethodDecider.isSeriesContributor())
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
		return (authorizeMethodDecider.isAdmin() || (isSeriesManagerWithStamp(seriesURI, getStamp()) && authorizeMethodDecider.isSeriesContributor()));
	}

	@Override
	public boolean canCreateSims(IRI seriesOrIndicatorUri) throws RmesException {
		return (authorizeMethodDecider.isAdmin() || (isSeriesManagerWithStamp(seriesOrIndicatorUri, getStamp()) && authorizeMethodDecider.isSeriesContributor())
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
		return ((isSeriesManagerWithStamp(uri, getStamp()) && authorizeMethodDecider.isSeriesContributor()) || authorizeMethodDecider.isAdmin() || authorizeMethodDecider.isCnis() );
	}

	@Override
	public boolean canModifyOperation(IRI seriesURI) throws RmesException {
		return canModifySeries(seriesURI);
	}

	@Override
	public boolean canValidateSeries(IRI uri) throws RmesException {
		return (authorizeMethodDecider.isAdmin() || (isSeriesManagerWithStamp(uri, getStamp()) && authorizeMethodDecider.isSeriesContributor()));
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


	private interface QueryGenerator {
		String generate(String query) throws RmesException;
	}
}
