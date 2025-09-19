package fr.insee.rmes.config.auth.user;

import fr.insee.rmes.infrastructure.rbac.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component("AuthorizeMethodDecider")
public record AuthorizeMethodDecider(PreAuthorizeChecker preAuthorizeChecker) {

	private static final Logger logger = LoggerFactory.getLogger(AuthorizeMethodDecider.class);

	public boolean isAdmin() {
		logger.debug("Check if user is {}", Roles.ADMIN);
		try {
			this.preAuthorizeChecker.hasRole(Roles.ADMIN);
			return true;
		} catch (AccessDeniedException denied) {
			return false;
		}
	}
}
