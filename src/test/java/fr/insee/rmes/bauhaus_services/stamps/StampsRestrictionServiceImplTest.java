package fr.insee.rmes.bauhaus_services.stamps;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.config.auth.UserProvider;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StampsRestrictionServiceImplTest {

    String timbre = "FR01-XXX";
    @Mock
    private RepositoryGestion repoGestion;
    @Mock
    private UserProvider userProvider;
    private final IRI iriToCheck = SimpleValueFactory.getInstance().createIRI("http://localhost/");

    @BeforeAll
    static void activateTraceLevel() throws URISyntaxException {
        var logbackConfigFilePath = Path.of(StampsRestrictionServiceImplTest.class
                .getClassLoader()
                .getResource(".")
                .toURI())
                .resolve("logback-test-trace.xml");

        System.setProperty("logback.configurationFile", logbackConfigFilePath.toString());
    }

    @BeforeAll
    static void configureOpSeriesQueries(){
        OpSeriesQueries.setConfig(new ConfigStub());
    }

    @Test
    void isSeriesManager_OK_whenStampIsManager() throws RmesException {
        var owners=new JSONArray();
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(owners);
        when(userProvider.findUserDefaultToEmpty()).thenReturn(new User("", List.of(), timbre));
        var stampsRestrictionServiceImpl = new StampsRestrictionServiceImpl(repoGestion, null, userProvider);
        assertTrue(stampsRestrictionServiceImpl.isSeriesManager(this.iriToCheck));
    }
}