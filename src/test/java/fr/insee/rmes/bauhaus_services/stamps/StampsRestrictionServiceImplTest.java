package fr.insee.rmes.bauhaus_services.stamps;

import fr.insee.rmes.bauhaus_services.accesscontrol.ResourceOwnershipByStampVerifierImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StampsRestrictionServiceImplTest {

    String timbre = "FR01-XXX";
    String timbre2 = "FR02-XXX";

    @Mock
    private RepositoryGestion repoGestion;
    private ResourceOwnershipByStampVerifierImpl stampsRestrictionsVerifier;
    @Mock
    private UserProvider userProvider;
    private final IRI iriToCheck = SimpleValueFactory.getInstance().createIRI("http://bauhaus/operations/serie/s2132");

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
    static void configureOpSeriesQueries() {
        OpSeriesQueries.setConfig(new ConfigStub());
    }

    @BeforeEach
    void injectRepositoryGestion() {
        stampsRestrictionsVerifier = new ResourceOwnershipByStampVerifierImpl(null, repoGestion, null);
    }

    @Test
    void isSeriesManager_OK_whenStampIsManagerWithMultipleCreators() throws RmesException {
        var owners=new JSONArray("[" +
                "{\"creators\":\""+timbre+"\"}," +
                "{\"creators\":\""+timbre2+"\"}" +
                "]");
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(owners);
        when(userProvider.findUserDefaultToEmpty()).thenReturn(new User("", List.of(), timbre));
        var stampsRestrictionServiceImpl = new StampsRestrictionServiceImpl(repoGestion, null, userProvider, stampsRestrictionsVerifier);
        assertTrue(stampsRestrictionServiceImpl.isSeriesManager(this.iriToCheck));
    }

    @Test
    void isSeriesManager_OK_whenStampIsManagerWithOneCreator() throws RmesException {
        var owners=new JSONArray("[" +
                "{\"creators\":\""+timbre+"\"}" +
                "]");
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(owners);
        when(userProvider.findUserDefaultToEmpty()).thenReturn(new User("", List.of(), timbre));
        var stampsRestrictionServiceImpl = new StampsRestrictionServiceImpl(repoGestion, null, userProvider, stampsRestrictionsVerifier);
        assertTrue(stampsRestrictionServiceImpl.isSeriesManager(this.iriToCheck));
    }

    @Test
    void isSeriesManager_KO_whenStampIsNotManagerWithOneCreator() throws RmesException {
        var owners=new JSONArray("[" +
                "{\"creators\":\""+timbre2+"\"}" +
                "]");
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(owners);
        when(userProvider.findUserDefaultToEmpty()).thenReturn(new User("", List.of(), timbre));
        var stampsRestrictionServiceImpl = new StampsRestrictionServiceImpl(repoGestion, null, userProvider, stampsRestrictionsVerifier);
        assertFalse(stampsRestrictionServiceImpl.isSeriesManager(this.iriToCheck));
    }

    @Test
    void isSeriesManager_KO_whenStampIsNotManagerWithMultipleCreator() throws RmesException {
        var owners=new JSONArray("[" +
                "{\"creators\":\""+timbre2+"\"}," +
                "{\"creators\":\""+timbre2+"\"}" +
                "]");
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(owners);
        when(userProvider.findUserDefaultToEmpty()).thenReturn(new User("", List.of(), timbre));
        var stampsRestrictionServiceImpl = new StampsRestrictionServiceImpl(repoGestion, null, userProvider, stampsRestrictionsVerifier);
        assertFalse(stampsRestrictionServiceImpl.isSeriesManager(this.iriToCheck));
    }
}