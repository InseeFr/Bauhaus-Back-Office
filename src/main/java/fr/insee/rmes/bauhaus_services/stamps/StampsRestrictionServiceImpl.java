package fr.insee.rmes.bauhaus_services.stamps;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
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
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;


@Service
public record StampsRestrictionServiceImpl(RepositoryGestion repoGestion, AuthorizeMethodDecider authorizeMethodDecider, UserProvider userProvider) implements StampsRestrictionsService {

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
		return isOwnerForModule(uris, ConceptsQueries::getOwner, Constants.OWNER);
	}

	private boolean isConceptManager(IRI uri) throws RmesException {
		return isManagerForModule(uri, ConceptsQueries::getManager, Constants.MANAGER);
	}


	public boolean isSeriesManager(IRI uri) throws RmesException {
		return isOwnerForModule(List.of(uri), OpSeriesQueries::getCreatorsBySeriesUri, Constants.CREATOR);
	}

	private boolean isIndicatorCreator(IRI iri) throws RmesException {
		return isOwnerForModule(List.of(iri), IndicatorsQueries::getCreatorsByIndicatorUri, Constants.CREATOR);
	}

	private boolean isManagerForModule(IRI uri, QueryGenerator queryGenerator, String stampKey) throws RmesException {
		return checkResponsabilityForModule(List.of(uri), queryGenerator, stampKey, Stream::anyMatch);
	}

	private boolean isOwnerForModule(List<IRI> uris, QueryGenerator queryGenerator, String stampKey) throws RmesException {
		return checkResponsabilityForModule(uris, queryGenerator, stampKey, Stream::allMatch);
	}

	private boolean checkResponsabilityForModule(List<IRI> uris, QueryGenerator queryGenerator, String stampKey, BiPredicate<Stream<String>, Predicate<String>> predicateMatcher) throws RmesException {
		JSONArray owners = repoGestion.getResponseAsArray(queryGenerator.generate(urisAsString(uris)));
		var stamp=getUser().stamp();
		return StringUtils.hasLength(stamp) &&
				predicateMatcher.test(owners.toList().stream()
								.map(JSONObject.class::cast)
								.map(o->o.getString(stampKey)),
						stamp::equals // apply predicate `stamp::equals` to the stream of stamps returned at the previous line
				);
	}

	@NotNull
	private String urisAsString(List<IRI> uris) {
		return uris.stream().map(this::uriAsString).reduce(String::concat).orElse("");
	}

	@NotNull
	private String uriAsString(IRI uri) {
		return "<" + RdfUtils.toString(uri) + ">";
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
		return (authorizeMethodDecider.isAdmin() || authorizeMethodDecider.isCnis() || (isSeriesManager(seriesOrIndicatorUri) && authorizeMethodDecider.isSeriesContributor())
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
		return (authorizeMethodDecider.isAdmin() || (isSeriesManager(seriesURI) && authorizeMethodDecider.isSeriesContributor()));
	}

	@Override
	public boolean canCreateSims(IRI seriesOrIndicatorUri) throws RmesException {
		return (authorizeMethodDecider.isAdmin() || (isSeriesManager(seriesOrIndicatorUri) && authorizeMethodDecider.isSeriesContributor())
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
		return ((isSeriesManager(uri) && authorizeMethodDecider.isSeriesContributor()) || authorizeMethodDecider.isAdmin() || authorizeMethodDecider.isCnis() );
	}

	@Override
	public boolean canModifyOperation(IRI seriesURI) throws RmesException {
		return canModifySeries(seriesURI);
	}

	@Override
	public boolean canValidateSeries(IRI uri) throws RmesException {
		return (authorizeMethodDecider.isAdmin() || (isSeriesManager(uri) && authorizeMethodDecider.isSeriesContributor()));
	}

	@Override
	public boolean canValidateOperation(IRI seriesURI) throws RmesException {
		return canValidateSeries(seriesURI);
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
