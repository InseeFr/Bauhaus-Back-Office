package fr.insee.rmes.persistance.service.sesame;

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
import fr.insee.rmes.persistance.service.sesame.concepts.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Service
public class StampsRestrictionsImpl implements StampsRestrictionsService {
	
	@Override
	public Boolean isConceptOrCollectionOwner(URI URI) throws Exception {
		if (!Config.ENV.equals("pre-prod") && !Config.ENV.equals("prod")) return true;
		User user = getUser();
		if (isAdmin(user)) return true;
		String URIAsString = "<" + URI.toString() + ">";
		JSONObject owner = RepositoryGestion.getResponseAsObject(ConceptsQueries.getOwner(URIAsString));
		Boolean isConceptOwner = true;
		if (!owner.getString("owner").equals(user.getStamp())) isConceptOwner = false;
		return isConceptOwner;
	}
	
	@Override
	public Boolean isConceptsOrCollectionsOwner(List<URI> URIs) throws Exception {
		if (!Config.ENV.equals("pre-prod") && !Config.ENV.equals("prod")) return true;
		User user = getUser();
		if (isAdmin(user)) return true;
		StringBuilder sb = new StringBuilder();
		URIs.forEach(u -> sb.append("<" + u.toString() + "> "));
		String URAsString = sb.toString();
		JSONArray owners = RepositoryGestion.getResponseAsArray(ConceptsQueries.getOwner(URAsString));
		Boolean isConceptsOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString("owner").equals(user.getStamp()))
				isConceptsOwner = false;
		}
		return isConceptsOwner;
	}
	
	private User getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();
		return user;
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

}
