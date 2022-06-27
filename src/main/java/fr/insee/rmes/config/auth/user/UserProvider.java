package fr.insee.rmes.config.auth.user;

import org.springframework.security.core.Authentication;

@FunctionalInterface
public interface UserProvider {

    User getUser(Authentication authentication);

}