package fr.insee.rmes.config.auth.security.keycloak;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import fr.insee.rmes.config.auth.security.manager.User;

public class KeycloakUserDetailsAuthenticationProvider extends KeycloakAuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserDetailsAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) super.authenticate(authentication);
        if (token == null) {
            return null;
        }

        final User user = new User();
        user.setRoles(new JSONArray(token.getAuthorities().toString()));

        final Map<String, Object> otherClaims = token.getAccount().getKeycloakSecurityContext().getToken().getOtherClaims();
        user.setStamp((String) otherClaims.getOrDefault("timbre", "default stamp"));
        
        String userId = token.getAccount().getKeycloakSecurityContext().getToken().getPreferredUsername();
        log.info("User " + userId + " connected");
        
        List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		user.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority((String) r)));

        return new KeycloakUserDetailsAuthenticationToken(user, token.getAccount(), token.getAuthorities());
    }

}
