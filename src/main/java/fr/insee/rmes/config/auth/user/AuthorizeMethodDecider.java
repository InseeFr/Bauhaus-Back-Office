package fr.insee.rmes.config.auth.user;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.roles.Roles;

@Component("AuthorizeMethodDecider")
public class AuthorizeMethodDecider {

	private static final Logger logger = LoggerFactory.getLogger(AuthorizeMethodDecider.class);

	private User fakeUser;

	@Autowired
	private UserProvider userProvider;

	@Autowired
	Config config;
	
	public User getUser() {
		if (config.getEnv().equals("pre-prod") || config.getEnv().equals("prod") || config.getEnv().equals("PROD")) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User currentUser = userProvider.getUser(authentication);
			logger.debug("Current user has stamp {}", currentUser == null ? "" : currentUser.getStamp());
			return currentUser;
		}
		return dvOrQfUser();
	}

	private User dvOrQfUser() {
		if (this.fakeUser != null) {
			return this.fakeUser;
		}

		JSONArray roles = new JSONArray();
		roles.put("ROLE_offline_access");
		roles.put("Administrateur_RMESGNCS");
		roles.put("ROLE_uma_authorization");
		return new User("fakeUser",roles, "fakeStampForDvAndQf");
	}


	public boolean isAdmin() {
		User user = getUser();
		return isAdmin(user);
	}

	public boolean isAdmin(User user) {
		logger.info("Check if user is admin");
		return hasRole(user,Roles.ADMIN);
	}
	
	public boolean isIndicatorContributor() {
		User user = getUser();
		return isIndicatorContributor(user);
	}
	
	public boolean isIndicatorContributor(User user) {		
		logger.info("Check if user is indicator's contributor");
		return hasRole(user,Roles.INDICATOR_CONTRIBUTOR);
	}
	
	public boolean isConceptsContributor() {
		User user = getUser();
		return isConceptsContributor(user);
	}
	public boolean isConceptsContributor(User user) {
		logger.info("Check if user is concepts' contributor");
		return hasRole(user,Roles.CONCEPTS_CONTRIBUTOR);
	}

	public boolean isConceptContributor() {
		User user = getUser();
		return isConceptContributor(user);
	}
	
	public boolean isConceptContributor(User user) {
		logger.info("Check if user is concept's contributor");
		return hasRole(user,Roles.CONCEPT_CONTRIBUTOR);
	}
	
	public boolean isConceptCreator() {
		User user = getUser();
		return isConceptCreator(user);
	}

	public boolean isConceptCreator(User user) {
		logger.info("Check if user is concept's creator");
		return hasRole(user,Roles.CONCEPT_CREATOR);
	}

	public boolean isSeriesContributor() {
		User user = getUser();
		return isSeriesContributor(user);
	}
	
	public boolean isSeriesContributor(User user) {
		logger.info("Check if user is series' contributor");
		return hasRole(user,Roles.SERIES_CONTRIBUTOR);
	}

	public boolean isCnis() {
		User user = getUser();
		return isCnis(user);
	}

	public boolean isCnis(User user) {
		logger.info("Check if user is CNIS' contributor");
		return hasRole(user,Roles.CNIS);
	}
	
	public boolean isCollectionCreator() {
		User user = getUser();
		return isCollectionCreator(user);
	}

	private boolean isCollectionCreator(User user) {
		logger.info("Check if user is collection's creator");
		return hasRole(user,Roles.COLLECTION_CREATOR);
	}
	
	private boolean hasRole(User user, String role) {
		Boolean hasRole = false;
		JSONArray roles = user.getRoles();
		for (int i = 0; i < roles.length(); i++) {
			if (roles.getString(i).equals(role)||roles.getString(i).equals(Roles.SPRING_PREFIX + role)) {
				hasRole = true;
			}
		}
		return hasRole;
	}


}
