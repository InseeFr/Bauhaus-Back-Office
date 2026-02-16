package fr.insee.rmes.keycloak;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@Qualifier("default")
public class KeycloakService extends AbstractTokenService {

    public KeycloakService(KeycloakProperties keycloakProperties) {
        super(keycloakProperties);
    }

    @Override
    protected KeycloakProperties.RealmConfig getRealmConfig() {
        return keycloakProperties.defaultrealm();
    }
}
