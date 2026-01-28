package fr.insee.rmes.keycloak;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("colectica")
public class ColecticaKeycloakService extends AbstractTokenService {

    public ColecticaKeycloakService(KeycloakProperties keycloakProperties) {
        super(keycloakProperties);
    }

    @Override
    protected String getRealmName() {
        return keycloakProperties.realm().colectica();
    }
}
