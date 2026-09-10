package fr.insee.rmes.keycloak;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

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
