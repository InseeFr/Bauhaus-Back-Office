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
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.sparqlQueries.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.sparqlQueries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparqlQueries.operations.series.SeriesQueries;

@Service
public class StampsRestrictionsImpl implements StampsRestrictionsService {

	@Override
	public Boolean isConceptOrCollectionOwner(URI uri) throws RmesException {
		User user = getUser();
		if (isAdmin(user)) {
			return true;
		}
		String uriAsString = "<" + uri.toString() + ">";
		JSONObject owner = RepositoryGestion.getResponseAsObject(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptOwner = true;
		if (!owner.getString("owner").equals(user.getStamp())) {
			isConceptOwner = false;
		}
		return isConceptOwner;
	}

	@Override
	public Boolean isConceptsOrCollectionsOwner(List<URI> uris) throws RmesException {
		User user = getUser();
		if (isAdmin(user)) {
			return true;
		}
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = RepositoryGestion.getResponseAsArray(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptsOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString("owner").equals(user.getStamp())) {
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
			if (roles.getString(i).equals(Roles.SPRING_ADMIN)) {
				isAdmin = true;
			}
		}
		return isAdmin;
	}

	private Boolean isConceptsContributor(User user) {
		Boolean isConceptsContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.CONCEPTS_CONTRIBUTOR)) {
				isConceptsContributor = true;
			}
		}
		return isConceptsContributor;
	}

	private Boolean isConceptContributor(User user) {
		Boolean isConceptContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.CONCEPT_CONTRIBUTOR)) {
				isConceptContributor = true;
			}
		}
		return isConceptContributor;
	}

	private Boolean isConceptCreator(User user) {
		Boolean isConceptCreator = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.CONCEPT_CREATOR)) {
				isConceptCreator = true;
			}
		}
		return isConceptCreator;
	}

	private Boolean isSeriesContributor(User user) {
		Boolean isSeriesContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.SERIES_CONTRIBUTOR)) {
				isSeriesContributor = true;
			}
		}
		return isSeriesContributor;
	}

	private Boolean isIndicatorContributor(User user) {
		Boolean isIndicatorContributor = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.INDICATOR_CONTRIBUTOR)) {
				isIndicatorContributor = true;
			}
		}
		return isIndicatorContributor;
	}

	private Boolean isCnis(User user) {
		Boolean isCnis = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(Roles.SPRING_CNIS)) {
				isCnis = true;
			}
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
			if (!owners.getJSONObject(i).getString("owner").equals(user.getStamp())) {
				isConceptOwner = false;
			}
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
			if (!managers.getJSONObject(i).getString("manager").equals(user.getStamp())) {
				isConceptManager = false;
			}
		}
		return isConceptManager;
	}


	private Boolean isSeriesManager(List<URI> uris) throws RmesException {
		for(URI uri:uris) {
			if (!isSeriesManager(uri)) {
				return false;
			}
		}
		return true;
	}

	private Boolean isSeriesManager(URI uri) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		sb.append("<" + uri.toString() + "> ");
		String uriAsString = sb.toString();
		JSONArray managers = RepositoryGestion.getResponseAsArray(SeriesQueries.getManagers(uriAsString));
		Boolean isSeriesManager = false;
		for (int i = 0; i < managers.length(); i++) {
			if (!managers.getJSONObject(i).getString("manager").equals(user.getStamp())) {
				isSeriesManager = true;
			}
		}
		return isSeriesManager;
	}

	private Boolean isIndicatorManager(List<URI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + u.toString() + "> "));
		String uriAsString = sb.toString();
		JSONArray managers = RepositoryGestion.getResponseAsArray(IndicatorsQueries.getManagers(uriAsString));
		Boolean isIndicatorManager = false;
		if (managers.length()==0) {
			isIndicatorManager = false;
		}
		for (int i = 0; i < managers.length(); i++) {
			if (!managers.getJSONObject(i).getString("manager").equals(user.getStamp())) {
				isIndicatorManager = true;
			}
		}
		return isIndicatorManager;
	}

	@Override
	public Boolean canModifyIndicator(List<URI> uris) throws RmesException {
		User user=getUser();
		return (isAdmin(user) || 
				(isIndicatorManager(uris) & isIndicatorContributor(user))
				);
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
		return (isAdmin(user) || 
				(isIndicatorManager(uris) & isIndicatorContributor(user))
				);
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
		return (isAdmin(user) ||
				isCnis(user) || 
				(isSeriesManager(uris) & isSeriesContributor(user)) || 
				(isIndicatorManager(uris) & isIndicatorContributor(user))
				);
	}

	@Override
	public Boolean canCreateConcept() throws RmesException {
		User user=getUser();
		return (isAdmin(user) || isConceptsContributor(user));
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
		return (isAdmin(user) || 
				(isSeriesManager(seriesURI) & isSeriesContributor(user))
				);
	}

	@Override
	public Boolean canCreateSims(URI targetURI) throws RmesException {
		List<URI> uris = new ArrayList<URI>();
		uris.add(targetURI);
		return canCreateSims(uris);
	}

	@Override
	public Boolean canCreateSims(List<URI> uris) throws RmesException {
		User user=getUser();
		return (isAdmin(user) |
				(isSeriesManager(uris) && isSeriesContributor(user)) || 
				(isIndicatorManager(uris) && isIndicatorContributor(user))
				);
	}

	@Override
	public Boolean canModifyConcept(URI uri) throws RmesException {
		User user=getUser();
		List<URI> uris = new ArrayList<URI>();
		uris.add(uri);
		return (isAdmin(user) || 
				isConceptsContributor(user) || 
				(isConceptManager(uris) & isConceptContributor(user)) || 
				(isConceptOwner(uris)) & isConceptCreator(user));
	}

	@Override
	public Boolean canModifySeries(URI uri) throws RmesException {
		User user=getUser();
		return (isAdmin(user) || 
				isCnis(user) || 
				(isSeriesManager(uri) & isSeriesContributor(user))
				);
	}

	@Override
	public Boolean canModifySeries(List<URI> uris) throws RmesException {
		User user=getUser();
		return (isAdmin(user) || 
				isCnis(user) || 
				(isSeriesManager(uris) & isSeriesContributor(user))
				);
	}

	@Override
	public Boolean canModifyOperation(URI seriesURI) throws RmesException {
		User user=getUser();
		return (isAdmin(user) || 
				isCnis(user) || 
				(isSeriesManager(seriesURI) & isSeriesContributor(user))
				);
	};

	@Override
	public Boolean canValidateSeries(URI uri) throws RmesException {
		User user=getUser();
		return (isAdmin(user) || 
				(isSeriesManager(uri) & isSeriesContributor(user))
				);
	}

	@Override
	public Boolean canValidateSeries(List<URI> uris) throws RmesException {
		User user=getUser();
		return (isAdmin(user) || 
				(isSeriesManager(uris) & isSeriesContributor(user))
				);
	}

	@Override
	public Boolean canValidateOperation(URI seriesURI) throws RmesException {
		User user=getUser();
		return (isAdmin(user) || 
				(isSeriesManager(seriesURI) & isSeriesContributor(user))
				);
	}

	@Override
	public Boolean canManageDocumentsAndLinks() throws RmesException {
		User user=getUser();
		return (isAdmin(user) || isSeriesContributor(user) || isIndicatorContributor(user));
	}

}
