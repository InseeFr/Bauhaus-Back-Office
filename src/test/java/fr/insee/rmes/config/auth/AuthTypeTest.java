package fr.insee.rmes.config.auth;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class AuthTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {"pre-prod", "prod","PROD" })
    void shouldGetAuthTypeReturnOpenIDConnectAuth(String env) {
        assertEquals("OpenIDConnectAuth", AuthType.getAuthType(env));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025", "might","PRODUCTION","prod ","Pre-prod"})
    void shouldGetAuthTypeReturnNoAuthImpl(String env) {
        assertEquals("NoAuthImpl", AuthType.getAuthType(env));
    }


}