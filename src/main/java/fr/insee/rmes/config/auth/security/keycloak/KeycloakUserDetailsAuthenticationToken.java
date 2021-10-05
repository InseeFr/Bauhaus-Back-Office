package fr.insee.rmes.config.auth.security.keycloak;

import java.util.Collection;
import java.util.Objects;

import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import fr.insee.rmes.config.auth.user.User;

public class KeycloakUserDetailsAuthenticationToken extends KeycloakAuthenticationToken {

    private static final long serialVersionUID = 1L;
    private final User user;

    public KeycloakUserDetailsAuthenticationToken(User user, OidcKeycloakAccount account, Collection<GrantedAuthority> authorities) {
        super(account, authorities);
        Assert.notNull(user, "User required");
        this.user = user;
        this.user.setAuthorities(authorities);
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(user);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeycloakUserDetailsAuthenticationToken other = (KeycloakUserDetailsAuthenticationToken) obj;
		return Objects.equals(user, other.user);
	}

}