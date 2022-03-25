package fr.insee.rmes.bauhaus_services.stamps;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;


@Service
public class StampsRestrictionServiceImpl implements StampsRestrictionsService {
	
	@Autowired
	protected RepositoryGestion repoGestion;
	
	static final Logger logger = LogManager.getLogger(StampsRestrictionServiceImpl.class);

	private User fakeUser;
	
	@Autowired
	static Config config;

	@Override
	public boolean isConceptOrCollectionOwner(IRI uri) throws RmesException {
		User user = getUser();
		if (isAdmin(user)) {
			return true;
		}
		String uriAsString = "<" + RdfUtils.toString(uri) + ">";
		JSONObject owner = repoGestion.getResponseAsObject(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptOwner = true;
		if (!owner.getString(Constants.OWNER).equals(user.getStamp())) {
			isConceptOwner = false;
		}
		return isConceptOwner;
	}

	@Override
	public boolean isConceptsOrCollectionsOwner(List<IRI> uris) throws RmesException {
		User user = getUser();
		if (isAdmin(user)) {
			return true;
		}
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + RdfUtils.toString(u) + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = repoGestion.getResponseAsArray(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptsOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString(Constants.OWNER).equals(user.getStamp())) {
				isConceptsOwner = false;
			}
		}
		return isConceptsOwner;
	}

	public User getUser() {
		if (config.getEnv().equals("pre-prod") || config.getEnv().equals("prod") ||  config.getEnv().equals("PROD")) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User currentUser = (User) authentication.getPrincipal();
			logger.info("Current user has stamp {}", currentUser.getStamp());
			return currentUser;
		}
		return dvOrQfUser();
	}


	private User dvOrQfUser() {
		if(this.fakeUser != null){
			return this.fakeUser;
		}

		JSONArray roles = new JSONArray();
		roles.put("ROLE_offline_access");
		roles.put("Administrateur_RMESGNCS");
		roles.put("ROLE_uma_authorization");
		return  new User(roles, "fakeStampForDvAndQf");
	}

	public void setFakeUser(String user) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JSONObject userObject = new JSONObject(user);

		JSONArray roles = userObject.getJSONArray("roles");

		this.fakeUser = new User(roles, userObject.getString(Constants.STAMP));
	}

	public boolean isAdmin() {
		User user = getUser();
		return (isAdmin(user));
	}
	
	private boolean isAdmin(User user) {
		Boolean isAdmin = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.ADMIN)||roles.getString(i).equals(Roles.SPRING_ADMIN)) {
				isAdmin = true;
			}
		}
		return isAdmin;
	}

	private boolean isConceptsContributor(User user) {
		Boolean isConceptsContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.CONCEPTS_CONTRIBUTOR)||roles.getString(i).equals(Roles.SPRING_CONCEPTS_CONTRIBUTOR)) {
				isConceptsContributor = true;
			}
		}
		return isConceptsContributor;
	}

	private boolean isConceptContributor(User user) {
		Boolean isConceptContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.CONCEPT_CONTRIBUTOR)||roles.getString(i).equals(Roles.SPRING_CONCEPT_CONTRIBUTOR)) {
				isConceptContributor = true;
			}
		}
		return isConceptContributor;
	}

	private boolean isConceptCreator(User user) {
		Boolean isConceptCreator = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.CONCEPT_CREATOR)||roles.getString(i).equals(Roles.SPRING_CONCEPT_CREATOR)) {
				isConceptCreator = true;
			}
		}
		return isConceptCreator;
	}

	private boolean isSeriesContributor(User user) {
		Boolean isSeriesContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.SERIES_CONTRIBUTOR)||roles.getString(i).equals(Roles.SPRING_SERIES_CONTRIBUTOR)) {
				isSeriesContributor = true;
			}
		}
		return isSeriesContributor;
	}

	private boolean isIndicatorContributor(User user) {
		Boolean isIndicatorContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.INDICATOR_CONTRIBUTOR)||roles.getString(i).equals(Roles.SPRING_INDICATOR_CONTRIBUTOR)) {
				isIndicatorContributor = true;
			}
		}
		return isIndicatorContributor;
	}

	private boolean isCnis(User user) {
		Boolean isCnis = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.CNIS)||roles.getString(i).equals(Roles.SPRING_CNIS)) {
				isCnis = true;
			}
		}
		return isCnis;
	}

	private boolean isConceptOwner(List<IRI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + RdfUtils.toString(u) + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = repoGestion.getResponseAsArray(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString(Constants.OWNER).equals(user.getStamp())) {
				isConceptOwner = false;
			}
		}
		return isConceptOwner;
	}

	private boolean isConceptManager(List<IRI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + RdfUtils.toString(u) + "> "));
		String uriAsString = sb.toString();
		JSONArray managers = repoGestion.getResponseAsArray(ConceptsQueries.getManager(uriAsString));
		Boolean isConceptManager = true;
		for (int i = 0; i < managers.length(); i++) {
			if (!managers.getJSONObject(i).getString(Constants.MANAGER).equals(user.getStamp())) {
				isConceptManager = false;
			}
		}
		return isConceptManager;
	}

	public boolean isSeriesManager(IRI uri) throws RmesException {
		User user = getUser();
		JSONArray managers = repoGestion.getResponseAsArray(OpSeriesQueries.getCreatorsBySeriesUri(RdfUtils.toString(uri)));
		Boolean isSeriesManager = false;
		for (int i = 0; i < managers.length(); i++) {
			if (managers.getJSONObject(i).getString(Constants.CREATORS).equals(user.getStamp())) {
				isSeriesManager = true;
			}
		}
		return isSeriesManager;
	}

	private boolean isIndicatorManager(IRI iri) throws RmesException {
		User user = getUser();
		String uriAsString = "<" + RdfUtils.toString(iri) + "> ";
		JSONArray creators = repoGestion.getResponseAsArray(IndicatorsQueries.getCreatorsByIndicatorUri(uriAsString));
		Boolean isIndicatorManager = false;
		if (creators.length() > 0) {
			for (int i = 0; i < creators.length(); i++) {
				if (creators.getJSONObject(i).getString(Constants.CREATORS).equals(user.getStamp())) {
					isIndicatorManager = true;
				}
			}
		}
		return isIndicatorManager;
	}

	private boolean canModifyOrValidateIndicator(IRI iri) throws RmesException {
		User user = getUser();
		return (isAdmin(user) || (isIndicatorManager(iri) && isIndicatorContributor(user)));
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
		User user = getUser();
		return (isAdmin(user) || isCnis(user) || (isSeriesManager(seriesOrIndicatorUri) && isSeriesContributor(user))
				|| (isIndicatorManager(seriesOrIndicatorUri) && isIndicatorContributor(user)));
	}

	@Override
	public boolean canCreateConcept() throws RmesException {
		User user = getUser();
		return (isAdmin(user) || isConceptsContributor(user));
	}

	@Override
	public boolean canCreateFamily() throws RmesException {
		return canCreateFamilySeriesOrIndicator();
	}

	private boolean canCreateFamilySeriesOrIndicator() {
		return isAdmin();
	}

	@Override
	public boolean canCreateSeries() throws RmesException {
		return canCreateFamilySeriesOrIndicator();
	}

	@Override
	public boolean canCreateIndicator() throws RmesException {
		return canCreateFamilySeriesOrIndicator();
	}

	@Override
	public boolean canCreateOperation(IRI seriesURI) throws RmesException {
		User user = getUser();
		return (isAdmin(user) || (isSeriesManager(seriesURI) && isSeriesContributor(user)));
	}

	@Override
	public boolean canCreateSims(IRI seriesOrIndicatorUri) throws RmesException {
		User user = getUser();
		return (isAdmin(user) || (isSeriesManager(seriesOrIndicatorUri) && isSeriesContributor(user))
				|| (isIndicatorManager(seriesOrIndicatorUri) && isIndicatorContributor(user)));
	}

	@Override
	public boolean canDeleteSims() throws RmesException {
		return isAdmin();
	}
	
	@Override
	public boolean canModifyConcept(IRI uri) throws RmesException {
		User user = getUser();
		List<IRI> uris = new ArrayList<>();
		uris.add(uri);
		return (isAdmin(user) || isConceptsContributor(user) || (isConceptManager(uris) && isConceptContributor(user))
				|| (isConceptOwner(uris)) && isConceptCreator(user));
	}

	@Override
	public boolean canModifySeries(IRI uri) throws RmesException {
		User user = getUser();
		return ((isSeriesManager(uri) && isSeriesContributor(user)) || isAdmin(user) || isCnis(user) );
	}

	@Override
	public boolean canModifyOperation(IRI seriesURI) throws RmesException {
		return canModifySeries(seriesURI);
	}

	@Override
	public boolean canValidateSeries(IRI uri) throws RmesException {
		User user = getUser();
		return (isAdmin(user) || (isSeriesManager(uri) && isSeriesContributor(user)));
	}

	@Override
	public boolean canValidateOperation(IRI seriesURI) throws RmesException {
		return canValidateSeries(seriesURI);
	}

	@Override
	public boolean canManageDocumentsAndLinks() throws RmesException {
		User user = getUser();
		return (isAdmin(user) || isSeriesContributor(user) || isIndicatorContributor(user));
	}

	@Override
	public boolean canValidateClassification(IRI uri) throws RmesException {
		return isAdmin();
	}

	

}
