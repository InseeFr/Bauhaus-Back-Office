package fr.insee.rmes.modules.users.infrastructure;

import fr.insee.rmes.modules.shared_kernel.domain.model.Roles;
import fr.insee.rmes.modules.users.domain.model.Source;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class DevAuthenticationFilter extends OncePerRequestFilter {

    /** En-têtes qui remplacent, pour une requête, les champs de l'utilisateur fictif (valeurs séparées par des virgules). */
    public static final String ROLES_HEADER = "X-Dev-User-Roles";

    public static final String STAMPS_HEADER = "X-Dev-User-Stamps";
    public static final String SOURCE_HEADER = "X-Dev-User-Source";

    private static final User DEFAULT_FAKE_USER =
            new User("fakeUser", List.of(Roles.ADMIN), Set.of(new Stamp("DG75-F302")), Source.INSEE);

    private final User fakeUser;

    public DevAuthenticationFilter() {
        this(DEFAULT_FAKE_USER);
    }

    public DevAuthenticationFilter(User fakeUser) {
        this.fakeUser = fakeUser;
    }

    /**
     * Utilisateur fictif décrit par {@code properties} ; chaque champ absent reprend la valeur de
     * l'utilisateur par défaut (administrateur, timbre DG75-F302, source INSEE).
     */
    public static User fakeUserFrom(DevUserProperties properties) {
        if (properties == null) {
            return DEFAULT_FAKE_USER;
        }
        String id = properties.id() == null || properties.id().isBlank() ? DEFAULT_FAKE_USER.id() : properties.id();
        List<String> roles = properties.roles() == null || properties.roles().isEmpty()
                ? DEFAULT_FAKE_USER.roles()
                : List.copyOf(properties.roles());
        Set<Stamp> stamps = properties.stamps() == null || properties.stamps().isEmpty()
                ? DEFAULT_FAKE_USER.stamps()
                : properties.stamps().stream().map(Stamp::new).collect(Collectors.toSet());
        Source source = properties.source() == null || properties.source().isBlank()
                ? DEFAULT_FAKE_USER.source()
                : Source.fromValue(properties.source());
        return new User(id, roles, stamps, source);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            User user = withHeaderOverrides(request);
            List<SimpleGrantedAuthority> authorities =
                    user.roles().stream().map(SimpleGrantedAuthority::new).toList();

            // Créer une authentification avec le User comme principal
            DevAuthentication authentication = new DevAuthentication(user, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /** L'utilisateur fictif, dont chaque champ présent dans les en-têtes {@code X-Dev-User-*} est remplacé. */
    private User withHeaderOverrides(HttpServletRequest request) {
        List<String> roles = headerValues(request, ROLES_HEADER);
        List<String> stamps = headerValues(request, STAMPS_HEADER);
        String source = request.getHeader(SOURCE_HEADER);
        return new User(
                fakeUser.id(),
                roles.isEmpty() ? fakeUser.roles() : roles,
                stamps.isEmpty()
                        ? fakeUser.stamps()
                        : stamps.stream().map(Stamp::new).collect(Collectors.toSet()),
                source == null || source.isBlank() ? fakeUser.source() : Source.fromValue(source.trim()));
    }

    private static List<String> headerValues(HttpServletRequest request, String header) {
        String value = request.getHeader(header);
        if (value == null) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .toList();
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
