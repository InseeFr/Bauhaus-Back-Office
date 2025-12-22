package fr.insee.rmes.modules.users.infrastructure;

import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.domain.auth.Source;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DevAuthenticationFilter extends OncePerRequestFilter {

    private static final User FAKE_USER = new User("fakeUser", List.of(Roles.ADMIN), Set.of(new Stamp("DG75-L201")), Source.SSM);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            List<SimpleGrantedAuthority> authorities = FAKE_USER.roles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            // Créer une authentification avec le User comme principal
            DevAuthentication authentication = new DevAuthentication(FAKE_USER, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // Classe interne pour l'authentification en mode DEV
    private static class DevAuthentication extends AbstractAuthenticationToken {
        private final User user;

        public DevAuthentication(User user, List<SimpleGrantedAuthority> authorities) {
            super(authorities);
            this.user = user;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return user; // Retourne directement le User
        }
    }
}