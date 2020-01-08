package fr.insee.rmes.persistance.service.sesame;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.URI;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.roles.Constants;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.concepts.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.service.sesame.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.service.sesame.operations.series.SeriesQueries;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Service
public class StampsRestrictionsImpl implements StampsRestrictionsService {

	@Override
	public Boolean isConceptOrCollectionOwner(URI uri) throws RmesException {
	//	if (!Config.ENV.equals("pre-prod") && !Config.ENV.equals("prod")) return true;
		User user = getUser();
		if (isAdmin(user)) return true;
		String uriAsString = "<" + uri.toString() + ">";
		JSONObject owner = RepositoryGestion.getResponseAsObject(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptOwner = true;
		if (!owner.getString("owner").equals(user.getStamp())) isConceptOwner = false;
		return isConceptOwner;
	}

	@Override
	public Boolean isConceptsOrCollectionsOwner(List<URI> uris) throws RmesException {
	//	if (!Config.ENV.equals("pre-prod") && !Config.ENV.equals("prod")) return true;
		User user = getUser();
		if (isAdmin(user)) return true;
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = RepositoryGestion.getResponseAsArray(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptsOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString("owner").equals(user.getStamp()))
				isConceptsOwner = false;
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
		JSONArray roles= new JSONArray();
		roles.put("ROLE_offline_access");	
		roles.put("ROLE_Administrateur_RMESGNCS");	
		roles.put("ROLE_uma_authorization");
		String stamp = "";
		User dvOrQfUser= new User(roles,stamp);
		return dvOrQfUser;
	}

	private Boolean isAdmin(User user) {
		Boolean isAdmin = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Constants.SPRING_ADMIN))
				isAdmin = true;
		}
		return isAdmin;
	}

	private Boolean isConceptsContributor(User user) {
		Boolean isConceptsContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Constants.CONCEPTS_CONTRIBUTOR))
				isConceptsContributor = true;
		}
		return isConceptsContributor;
	}

	private Boolean isSeriesContributor(User user) {
		Boolean isSeriesContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Constants.SERIES_CONTRIBUTOR))
				isSeriesContributor = true;
		}
		return isSeriesContributor;
	}

	private Boolean isIndicatorContributor(User user) {
		Boolean isIndicatorContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Constants.INDICATOR_CONTRIBUTOR))
				isIndicatorContributor = true;
		}
		return isIndicatorContributor;
	}


	private Boolean isCnis(User user) {
		Boolean isCnis = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Constants.SPRING_CNIS))
				isCnis = true;
		}
		return isCnis;
	}

	private Boolean isConceptOwner(List<URI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = RepositoryGestion.getResponseAsArray(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString("owner").equals(user.getStamp()))
				isConceptOwner = false;
		}
		return isConceptOwner;
	}

	private Boolean isConceptManager(List<URI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray managers = RepositoryGestion.getResponseAsArray(ConceptsQueries.getManager(uriAsString));
		Boolean isConceptManager = true;
		for (int i = 0; i < managers.length(); i++) {
			if (!managers.getJSONObject(i).getString("manager").equals(user.getStamp()))
				isConceptManager = false;
		}
		return isConceptManager;
	}


	private Boolean isSeriesOwner(List<URI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = RepositoryGestion.getResponseAsArray(SeriesQueries.getOwner(uriAsString));
		Boolean isSeriesOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString("owner").equals(user.getStamp()))
				isSeriesOwner = false;
		}
		return isSeriesOwner;
	}

	private Boolean isIndicatorOwner(List<URI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = RepositoryGestion.getResponseAsArray(IndicatorsQueries.getOwner(uriAsString));
		Boolean isIndicatorOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString("owner").equals(user.getStamp()))
				isIndicatorOwner = false;
		}
		return isIndicatorOwner;
	}

	private Boolean isSeriesManager(List<URI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray managers = RepositoryGestion.getResponseAsArray(SeriesQueries.getManager(uriAsString));
		Boolean isSeriesManager = true;
		if (managers.length()==0) isSeriesManager = false;
		for (int i = 0; i < managers.length(); i++) {
			if (!managers.getJSONObject(i).getString("manager").equals(user.getStamp()))
				isSeriesManager = false;
		}
		return isSeriesManager;
	}

	private Boolean isIndicatorManager(List<URI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray managers = RepositoryGestion.getResponseAsArray(IndicatorsQueries.getManager(uriAsString));
		Boolean isIndicatorManager = true;
		if (managers.length()==0) isIndicatorManager = false;
		for (int i = 0; i < managers.length(); i++) {
			if (!managers.getJSONObject(i).getString("manager").equals(user.getStamp()))
				isIndicatorManager = false;
		}
		return isIndicatorManager;
	}

	@Override
	public Boolean canModifyIndicator(List<URI> uris) throws RmesException {
		User user=getUser();
		return (isAdmin(user) | isIndicatorManager(uris));
	}

	@Override
	public Boolean canModifyIndicator(URI uri) throws RmesException {
		List<URI> uris = new ArrayList<URI>();
		uris.add(uri);
		return canModifyIndicator(uris);
	}

	@Override
	public Boolean canValidateIndicator(List<URI> uris) throws RmesException {
		User user=getUser();
		return (isAdmin(user) | isIndicatorManager(uris));
	}

	@Override
	public Boolean canValidateIndicator(URI uri) throws RmesException {
		List<URI> uris = new ArrayList<URI>();
		uris.add(uri);
		return canValidateIndicator(uris);
	}

	@Override
	public Boolean canModifySims(URI targetURI) throws RmesException {
		User user=getUser();
		List<URI> uris = new ArrayList<URI>();
		uris.add(targetURI);
		return (isAdmin(user) | isCnis(user) | isSeriesManager(uris) | isIndicatorManager(uris));
	}

	@Override
	public Boolean canCreateConcept() throws RmesException {
		User user=getUser();
		return (isAdmin(user) | isConceptsContributor(user));
	}

	@Override
	public Boolean canCreateFamily() throws RmesException {
		User user=getUser();
		return (isAdmin(user));
	}

	@Override
	public Boolean canCreateSeries() throws RmesException {
		User user=getUser();
		return (isAdmin(user));
	}

	@Override
	public Boolean canCreateIndicator() throws RmesException {
		User user=getUser();
		return (isAdmin(user));
	}	

	@Override
	public Boolean canCreateOperation(URI seriesURI) throws RmesException {
		User user=getUser();
		List<URI> uris = new ArrayList<URI>();
		uris.add(seriesURI);
		return (isAdmin(user) | isSeriesManager(uris));
	}

	@Override
	public Boolean canCreateSims(URI targetURI) throws RmesException {
		User user=getUser();
		List<URI> uris = new ArrayList<URI>();
		uris.add(targetURI);
		return (isAdmin(user) | isSeriesManager(uris) | isIndicatorManager(uris));
	}


	@Override
	public Boolean canModifyConcept(URI uri) throws RmesException {
		User user=getUser();
		List<URI> uris = new ArrayList<URI>();
		uris.add(uri);
		return (isAdmin(user) | isConceptsContributor(user) | isConceptManager(uris) | isConceptOwner(uris));
	}

	@Override
	public Boolean canCreateSims(List<URI> uris) throws RmesException {
		User user=getUser();
		return (isAdmin(user) | isSeriesManager(uris) | isIndicatorManager(uris));
	}

	@Override
	public Boolean canModifySeries(URI uri) throws RmesException {
		List<URI> uris = new ArrayList<URI>();
		uris.add(uri);
		return canModifySeries(uris);
	}

	@Override
	public Boolean canModifySeries(List<URI> uris) throws RmesException {
		User user=getUser();
		return (isAdmin(user) | isCnis(user) | isSeriesManager(uris));
	}

	public Boolean canModifyOperation(URI seriesURI) throws RmesException {
		List<URI> uris = new ArrayList<URI>();
		uris.add(seriesURI);
		User user=getUser();
		return (isAdmin(user) | isCnis(user) | isSeriesManager(uris));
	};


	@Override
	public Boolean canValidateSeries(URI uri) throws RmesException {
		List<URI> uris = new ArrayList<URI>();
		uris.add(uri);
		return canValidateSeries(uris);
	}

	@Override
	public Boolean canValidateSeries(List<URI> uris) throws RmesException {
		User user=getUser();
		return (isAdmin(user) | isSeriesManager(uris));
	}

	@Override
	public Boolean canValidateOperation(URI seriesURI) throws RmesException {
		List<URI> uris = new ArrayList<URI>();
		uris.add(seriesURI);
		User user=getUser();
		return (isAdmin(user) | isSeriesManager(uris));
	}

	@Override
	public Boolean canManageDocumentsAndLinks() throws RmesException {
		User user=getUser();
		return (isAdmin(user) | isSeriesContributor(user) |isIndicatorContributor(user));
	}


}
