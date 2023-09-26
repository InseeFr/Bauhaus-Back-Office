package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.webservice.HealthcheckApi;
import fr.insee.rmes.webservice.PublicResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers= HealthcheckApi.class )
class HealthcheckAuthorizationsTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void ok_withoutAuth(String endpoint){
        // TODO : cf. PublicResourcesAuthorizationsTest
    }

}
