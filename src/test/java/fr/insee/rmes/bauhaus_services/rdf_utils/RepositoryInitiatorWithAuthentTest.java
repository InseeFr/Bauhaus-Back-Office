package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.keycloak.KeycloakServices;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryInitiatorWithAuthentTest {

    @Mock
    private KeycloakServices keycloakServices;
    private Repository repository;

    @Test
    void initRepository_test() throws RmesException {
        var repositoryInitiatorWithAuthent = new RepositoryInitiatorWithAuthent(keycloakServices);
        when(keycloakServices.isTokenValid(null)).thenReturn(Boolean.FALSE);
        when(keycloakServices.isTokenValid(anyString())).thenReturn(Boolean.TRUE);
        when(keycloakServices.getKeycloakAccessToken(anyString())).thenReturn("token");
        var servers= List.of("http://server1", "http://server2", "http://server3", "http://server1", "http://server1", "http://server3");
        for (String server : servers){
            assertDoesNotThrow(()-> repository = repositoryInitiatorWithAuthent.initRepository(server, "id"));
            assertAll(()->assertTrue(repository.isInitialized()),
                    ()->assertInstanceOf(HTTPRepository.class, repository),
                    ()->assertEquals(server+"/repositories/id", ((HTTPRepository) repository).getRepositoryURL())
            );
        }

    }
}