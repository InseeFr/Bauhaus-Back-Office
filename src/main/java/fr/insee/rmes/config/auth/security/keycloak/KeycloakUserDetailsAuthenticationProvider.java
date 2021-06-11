package fr.insee.rmes.config.auth.security.keycloak;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import fr.insee.rmes.config.auth.user.User;

public class KeycloakUserDetailsAuthenticationProvider extends KeycloakAuthenticationProvider {

    private static final Logger log = LogManager.getLogger(KeycloakUserDetailsAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) {
        final KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) super.authenticate(authentication);
        if (token == null) {
            return null;
        }

        final User user = new User();
        user.setRoles(new JSONArray(token.getAuthorities().toString()));

        final Map<String, Object> otherClaims = token.getAccount().getKeycloakSecurityContext().getToken().getOtherClaims();
        user.setStamp((String) otherClaims.getOrDefault("timbre", "default stamp"));
        
        String userId = token.getAccount().getKeycloakSecurityContext().getToken().getPreferredUsername();
        log.info("User {} connected with roles {} and stamps {}", userId, user.getRoles(), user.getStamp());
        
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		user.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority((String) r)));

        return new KeycloakUserDetailsAuthenticationToken(user, token.getAccount(), token.getAuthorities());
    }

}
