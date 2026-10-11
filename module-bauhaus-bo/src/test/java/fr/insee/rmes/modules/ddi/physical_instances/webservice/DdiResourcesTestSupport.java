package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.ResponseEntity;

/** Décors et assertions partagés par les tests unitaires des contrôleurs DDI. */
final class DdiResourcesTestSupport {

    private DdiResourcesTestSupport() {}

    /** L'utilisateur courant porte le timbre {@code stamp-A} et lit les instances physiques par timbre. */
    static void givenStampUserReadingPhysicalInstances(UserProvider userProvider, RbacFetcher rbacFetcher)
            throws MissingUserInformationException, RmesException {
        User stampUser = new User("user-1", List.of("role-stamp"), Set.of("stamp-A"));
        when(userProvider.findUser()).thenReturn(Optional.of(stampUser));
        when(rbacFetcher.getApplicationActionStrategyByRole(
                        any(), eq(RBAC.Module.DDI_PHYSICALINSTANCE), eq(RBAC.Privilege.READ)))
                .thenReturn(RBAC.Strategy.STAMP);
    }

    /** La réponse est un 200 dont le corps est une liste de {@code expectedSize} éléments, rendue pour la suite. */
    static <T> List<T> assertOkListOfSize(ResponseEntity<List<T>> response, int expectedSize) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        return assertBodyOfSize(response, expectedSize);
    }

    /** Le corps de la réponse est une liste de {@code expectedSize} éléments, rendue pour la suite. */
    static <T> List<T> assertBodyOfSize(ResponseEntity<List<T>> response, int expectedSize) {
        List<T> body = response.getBody();
        assertNotNull(body);
        assertEquals(expectedSize, body.size());
        return body;
    }
}
