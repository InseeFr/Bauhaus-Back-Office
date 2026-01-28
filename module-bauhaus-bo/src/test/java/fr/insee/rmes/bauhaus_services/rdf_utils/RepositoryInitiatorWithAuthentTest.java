package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.graphdb.RepositoryInitiatorWithAuthent;
import fr.insee.rmes.keycloak.TokenService;
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
    private TokenService tokenService;
    private Repository repository;

    @Test
    void initRepository_test() {
        var repositoryInitiatorWithAuthent = new RepositoryInitiatorWithAuthent(tokenService);
        when(tokenService.isTokenValid(null)).thenReturn(Boolean.FALSE);
        when(tokenService.isTokenValid(anyString())).thenReturn(Boolean.TRUE);
        when(tokenService.getAccessToken()).thenReturn("token");
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