package fr.insee.rmes.config.auth;

import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.RoleClaimExtractor;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Configuration de test regroupant les composants d'authentification utilisateur.
 * À importer dans les tests d'intégration qui nécessitent l'authentification OIDC.
 */
@Import({
        UserProviderFromSecurityContext.class,
        OidcUserDecoder.class,
        RoleClaimExtractor.class
})
public class UserAuthTestConfiguration {
}
