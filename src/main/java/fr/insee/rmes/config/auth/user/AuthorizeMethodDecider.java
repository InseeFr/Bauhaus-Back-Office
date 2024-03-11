package fr.insee.rmes.config.auth.user;

import fr.insee.rmes.config.auth.roles.Roles;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("AuthorizeMethodDecider")
public record AuthorizeMethodDecider(PreAuthorizeChecker preAuthorizeChecker) {

	private static final Logger logger = LoggerFactory.getLogger(AuthorizeMethodDecider.class);

	public boolean isAdmin() {
		return checkIfHasRole(Roles.ADMIN);
	}

	private boolean checkIfHasRole(String roleToCheck) {
		logger.debug("Check if user is {}", roleToCheck);
		try {
			this.preAuthorizeChecker.hasRole(roleToCheck);
			return true;
		} catch (AccessDeniedException denied) {
			return false;
		}
	}


	public boolean isIndicatorContributor() {
		return checkIfHasRole(Roles.INDICATOR_CONTRIBUTOR);
	}

	public boolean isConceptsContributor() {
		return checkIfHasRole(Roles.CONCEPTS_CONTRIBUTOR);
	}

	
	public boolean isConceptContributor() {
		return checkIfHasRole(Roles.CONCEPT_CONTRIBUTOR);
	}


	public boolean isConceptCreator() {
		return checkIfHasRole(Roles.CONCEPT_CREATOR);
	}
	
	public boolean isSeriesContributor() {
		return checkIfHasRole(Roles.SERIES_CONTRIBUTOR);
	}

	public boolean isCnis() {
		return checkIfHasRole(Roles.CNIS);
	}


}
