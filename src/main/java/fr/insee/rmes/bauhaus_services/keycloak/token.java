package fr.insee.rmes.bauhaus_services.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

public class token {
    @JsonProperty("access_token")
    String access_token;

    private String token_type;

    public String getAccessToken() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }
}
