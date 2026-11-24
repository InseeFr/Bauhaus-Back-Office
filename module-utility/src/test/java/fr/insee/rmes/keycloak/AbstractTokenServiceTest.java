package fr.insee.rmes.keycloak;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClient;

/** Wires {@code service} to a mocked Keycloak client whose token request answers {@code responseSpec}. */
abstract class AbstractTokenServiceTest<S extends AbstractTokenService> {

    @Mock
    protected RestClient testRestClient;

    @Mock
    protected RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock(answer = Answers.RETURNS_SELF)
    protected RestClient.RequestBodySpec requestBodySpec;

    @Mock
    protected RestClient.ResponseSpec responseSpec;

    protected S service;

    abstract S createService(KeycloakProperties properties);

    @BeforeEach
    void setUpKeycloakClient() {
        MockitoAnnotations.openMocks(this);
        service = createService(KeycloakTestFixtures.properties());
        service.keycloakClient = testRestClient;

        when(testRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Token.class)).thenReturn(KeycloakTestFixtures.token("token"));
    }
}
