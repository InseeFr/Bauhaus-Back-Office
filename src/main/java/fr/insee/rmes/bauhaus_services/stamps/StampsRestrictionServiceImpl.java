package fr.insee.rmes.bauhaus_services.stamps;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.SeriesQueries;

@Service
public class StampsRestrictionServiceImpl extends RdfService implements StampsRestrictionsService {

	private static final String MANAGER = "manager";
	private static final String OWNER = "owner";

	@Override
	public boolean isConceptOrCollectionOwner(IRI uri) throws RmesException {
		User user = getUser();
		if (isAdmin(user)) {
			return true;
		}
		String uriAsString = "<" + uri.toString() + ">";
		JSONObject owner = repoGestion.getResponseAsObject(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptOwner = true;
		if (!owner.getString(OWNER).equals(user.getStamp())) {
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
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = repoGestion.getResponseAsArray(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptsOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString(OWNER).equals(user.getStamp())) {
				isConceptsOwner = false;
			}
		}
		return isConceptsOwner;
	}

	private User getUser() {
		if (Config.ENV.equals("pre-prod") || Config.ENV.equals("prod")) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			return (User) authentication.getPrincipal();
		}
		return dvOrQfUser();
	}

	private User dvOrQfUser() {
		JSONArray roles = new JSONArray();
		roles.put("ROLE_offline_access");
		roles.put("ROLE_Administrateur_RMESGNCS");
		roles.put("ROLE_uma_authorization");
		return new User(roles, "");
	}

	private boolean isAdmin(User user) {
		Boolean isAdmin = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.SPRING_ADMIN)) {
				isAdmin = true;
			}
		}
		return isAdmin;
	}

	private boolean isConceptsContributor(User user) {
		Boolean isConceptsContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.CONCEPTS_CONTRIBUTOR)) {
				isConceptsContributor = true;
			}
		}
		return isConceptsContributor;
	}

	private boolean isConceptContributor(User user) {
		Boolean isConceptContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.CONCEPT_CONTRIBUTOR)) {
				isConceptContributor = true;
			}
		}
		return isConceptContributor;
	}

	private boolean isConceptCreator(User user) {
		Boolean isConceptCreator = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.CONCEPT_CREATOR)) {
				isConceptCreator = true;
			}
		}
		return isConceptCreator;
	}

	private boolean isSeriesContributor(User user) {
		Boolean isSeriesContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.SERIES_CONTRIBUTOR)) {
				isSeriesContributor = true;
			}
		}
		return isSeriesContributor;
	}

	private boolean isIndicatorContributor(User user) {
		Boolean isIndicatorContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.INDICATOR_CONTRIBUTOR)) {
				isIndicatorContributor = true;
			}
		}
		return isIndicatorContributor;
	}

	private boolean isCnis(User user) {
		Boolean isCnis = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.SPRING_CNIS)) {
				isCnis = true;
			}
		}
		return isCnis;
	}

	private boolean isConceptOwner(List<IRI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = repoGestion.getResponseAsArray(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString(OWNER).equals(user.getStamp())) {
				isConceptOwner = false;
			}
		}
		return isConceptOwner;
	}

	private boolean isConceptManager(List<IRI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray managers = repoGestion.getResponseAsArray(ConceptsQueries.getManager(uriAsString));
		Boolean isConceptManager = true;
		for (int i = 0; i < managers.length(); i++) {
			if (!managers.getJSONObject(i).getString(MANAGER).equals(user.getStamp())) {
				isConceptManager = false;
			}
		}
		return isConceptManager;
	}

	private boolean isSeriesManager(List<IRI> uris) throws RmesException {
		for (IRI uri : uris) {
			if (!isSeriesManager(uri)) {
				return false;
			}
		}
		return true;
	}

	private boolean isSeriesManager(IRI uri) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		sb.append("<" + uri.toString() + "> ");
		String uriAsString = sb.toString();
		JSONArray managers = repoGestion.getResponseAsArray(SeriesQueries.getManagers(uriAsString));
		Boolean isSeriesManager = false;
		for (int i = 0; i < managers.length(); i++) {
			if (!managers.getJSONObject(i).getString(MANAGER).equals(user.getStamp())) {
				isSeriesManager = true;
			}
		}
		return isSeriesManager;
	}

	private boolean isIndicatorManager(List<IRI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray managers = repoGestion.getResponseAsArray(IndicatorsQueries.getManagers(uriAsString));
		Boolean isIndicatorManager = false;
		if (managers.length() > 0) {
			for (int i = 0; i < managers.length(); i++) {
				if (!managers.getJSONObject(i).getString(MANAGER).equals(user.getStamp())) {
					isIndicatorManager = true;
				}
			}
		}
		return isIndicatorManager;
	}

	private boolean canModifyOrValidateIndicator(List<IRI> uris) throws RmesException {
		User user = getUser();
		return (isAdmin(user) || (isIndicatorManager(uris) && isIndicatorContributor(user)));
	}

	@Override
	public boolean canModifyIndicator(List<IRI> uris) throws RmesException {
		return canModifyOrValidateIndicator(uris);
	}

	@Override
	public boolean canValidateIndicator(List<IRI> uris) throws RmesException {
		return canModifyOrValidateIndicator(uris);
	}

	@Override
	public boolean canModifyIndicator(IRI uri) throws RmesException {
		List<IRI> uris = new ArrayList<>();
		uris.add(uri);
		return canModifyIndicator(uris);
	}

	@Override
	public boolean canValidateIndicator(IRI uri) throws RmesException {
		List<IRI> uris = new ArrayList<>();
		uris.add(uri);
		return canValidateIndicator(uris);
	}

	@Override
	public boolean canModifySims(IRI targetURI) throws RmesException {
		User user = getUser();
		List<IRI> uris = new ArrayList<>();
		uris.add(targetURI);
		return (isAdmin(user) || isCnis(user) || (isSeriesManager(uris) && isSeriesContributor(user))
				|| (isIndicatorManager(uris) && isIndicatorContributor(user)));
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
		User user = getUser();
		return (isAdmin(user));
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
	public boolean canCreateSims(IRI targetURI) throws RmesException {
		List<IRI> uris = new ArrayList<>();
		uris.add(targetURI);
		return canCreateSims(uris);
	}

	@Override
	public boolean canCreateSims(List<IRI> uris) throws RmesException {
		User user = getUser();
		return (isAdmin(user) || (isSeriesManager(uris) && isSeriesContributor(user))
				|| (isIndicatorManager(uris) && isIndicatorContributor(user)));
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
		return (isAdmin(user) || isCnis(user) || (isSeriesManager(uri) && isSeriesContributor(user)));
	}

	@Override
	public boolean canModifySeries(List<IRI> uris) throws RmesException {
		User user = getUser();
		return (isAdmin(user) || isCnis(user) || (isSeriesManager(uris) && isSeriesContributor(user)));
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
	public boolean canValidateSeries(List<IRI> uris) throws RmesException {
		User user = getUser();
		return (isAdmin(user) || (isSeriesManager(uris) && isSeriesContributor(user)));
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

}
