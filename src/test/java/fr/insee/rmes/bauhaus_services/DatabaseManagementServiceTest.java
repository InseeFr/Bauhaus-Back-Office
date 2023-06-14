package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class DatabaseManagementServiceTest {

    @Mock
    Config config;

    @Mock
    RepositoryGestion repoGestion;


    @InjectMocks
    private DatabaseManagementServiceImpl databaseManagementService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldThrowAnErrorOnProdEnvironment() {
        when(config.getEnv()).thenReturn("prod");
        Assertions.assertThrows(RmesBadRequestException.class, () -> {
            databaseManagementService.clearGraph();
        });
    }

    @Test
    void shouldCallRepoGestionClearGraph() throws RmesBadRequestException {
        when(config.getEnv()).thenReturn("pre-prod");
        databaseManagementService.clearGraph();
        verify(repoGestion, times(1)).clearGraph();
    }
}
