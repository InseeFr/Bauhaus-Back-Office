package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.config.Config;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RepositoryGestionTest {

    @InjectMocks
    RepositoryGestion repositoryGestion;

    @Mock
    Config config;

    @Mock
    RepositoryUtils repositoryUtils;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCallRepositoryClear() {
        when(config.getRdfServerGestion()).thenReturn("rdfServerGestion");
        when(config.getRepositoryIdGestion()).thenReturn("repositoryIdGestion");

        Repository mockRepository = mock(Repository.class);
        RepositoryConnection mockRepositoryConnection = mock(RepositoryConnection.class);

        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(mockRepository);
        when(mockRepository.getConnection()).thenReturn(mockRepositoryConnection);
        repositoryGestion.clearGraph();

        verify(mockRepositoryConnection, times(1)).clear();
    }

}
