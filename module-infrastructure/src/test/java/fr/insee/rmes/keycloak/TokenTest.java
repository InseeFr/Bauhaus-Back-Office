package fr.insee.rmes.keycloak;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    Token token = new Token();

    @Test
    void shouldReturnNullWhenGetAccessToken() {
        assertNull(token.getAccessToken());
    }

    @Test
    void shouldReturnNullWhenGetToken_type() {
        assertNull(token.getToken_type());
    }
}