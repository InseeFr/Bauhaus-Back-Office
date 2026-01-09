package fr.insee.rmes.modules.commons.webservice;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicResourcesTest {

    private PublicResources publicResources;

    @Mock
    private ColecticaConfiguration colecticaConfiguration;

    @Mock
    private ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;

    @BeforeEach
    void setUp() {
        when(colecticaConfiguration.server()).thenReturn(instanceConfiguration);
        when(instanceConfiguration.defaultAgencyId()).thenReturn("fr.insee");

        publicResources = new PublicResources(
                "dev",
                "fr",
                "en",
                "350",
                "DG75-L201",
                "http://localhost:3000",
                List.of("concepts", "classifications"),
                List.of("concepts", "classifications", "operations"),
                "1.0.0",
                List.of("altLabel"),
                colecticaConfiguration
        );
    }

    @Test
    void shouldReturnPropertiesWithAllFields() throws RmesException {
        ResponseEntity<String> response = publicResources.getProperties();

        assertEquals(200, response.getStatusCode().value());

        JSONObject props = new JSONObject(response.getBody());
        assertEquals("http://localhost:3000", props.getString("appHost"));
        assertEquals("DG75-L201", props.getString("defaultContributor"));
        assertEquals("350", props.getString("maxLengthScopeNote"));
        assertEquals("fr", props.getString("lg1"));
        assertEquals("en", props.getString("lg2"));
        assertEquals("NoAuthImpl", props.getString("authType"));
        assertEquals("1.0.0", props.getString("version"));
        assertEquals("fr.insee", props.getString("defaultAgencyId"));
        assertTrue(props.getJSONArray("activeModules").toList().contains("concepts"));
        assertTrue(props.getJSONArray("modules").toList().contains("operations"));
        assertTrue(props.getJSONArray("extraMandatoryFields").toList().contains("altLabel"));
    }

    @Test
    void shouldReturnOpenIDConnectAuthForPreProd() throws RmesException {
        publicResources = new PublicResources(
                "pre-prod",
                "fr",
                "en",
                "350",
                "DG75-L201",
                "http://localhost:3000",
                List.of(),
                List.of(),
                "1.0.0",
                List.of(),
                colecticaConfiguration
        );

        ResponseEntity<String> response = publicResources.getProperties();
        JSONObject props = new JSONObject(response.getBody());

        assertEquals("OpenIDConnectAuth", props.getString("authType"));
    }

    @Test
    void shouldReturnOpenIDConnectAuthForProd() throws RmesException {
        publicResources = new PublicResources(
                "prod",
                "fr",
                "en",
                "350",
                "DG75-L201",
                "http://localhost:3000",
                List.of(),
                List.of(),
                "1.0.0",
                List.of(),
                colecticaConfiguration
        );

        ResponseEntity<String> response = publicResources.getProperties();
        JSONObject props = new JSONObject(response.getBody());

        assertEquals("OpenIDConnectAuth", props.getString("authType"));
    }

    @Test
    void shouldReturnOpenIDConnectAuthForPROD() throws RmesException {
        publicResources = new PublicResources(
                "PROD",
                "fr",
                "en",
                "350",
                "DG75-L201",
                "http://localhost:3000",
                List.of(),
                List.of(),
                "1.0.0",
                List.of(),
                colecticaConfiguration
        );

        ResponseEntity<String> response = publicResources.getProperties();
        JSONObject props = new JSONObject(response.getBody());

        assertEquals("OpenIDConnectAuth", props.getString("authType"));
    }

    @Test
    void shouldReturnNoAuthImplForDevEnvironment() throws RmesException {
        ResponseEntity<String> response = publicResources.getProperties();
        JSONObject props = new JSONObject(response.getBody());

        assertEquals("NoAuthImpl", props.getString("authType"));
    }
}