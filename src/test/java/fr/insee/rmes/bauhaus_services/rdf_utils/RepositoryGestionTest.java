package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.config.Config;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

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
    void shouldCallRepositoryClear() throws IOException {
        when(config.getRdfServerGestion()).thenReturn("rdfServerGestion");
        when(config.getRepositoryIdGestion()).thenReturn("repositoryIdGestion");

        Repository mockRepository = mock(Repository.class);
        RepositoryConnection mockRepositoryConnection = mock(RepositoryConnection.class);

        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(mockRepository);
        when(mockRepository.getConnection()).thenReturn(mockRepositoryConnection);
        repositoryGestion.clearGraph(new File(""));

       verify(mockRepositoryConnection, times(1)).clear();
       verify(mockRepositoryConnection, times(1)).add(any(File.class), eq(RDFFormat.TRIG));
    }

}
