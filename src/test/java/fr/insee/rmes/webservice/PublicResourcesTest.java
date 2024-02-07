package fr.insee.rmes.webservice;

import fr.insee.rmes.exceptions.RmesException;
import org.assertj.core.api.Assert;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@SpringBootTest({
        "fr.insee.rmes.bauhaus.env=dev",
        "fr.insee.rmes.bauhaus.lg1=fr",
        "fr.insee.rmes.bauhaus.lg2=en",
        "fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote=35",
        "fr.insee.rmes.bauhaus.concepts.defaultMailSender=email",
        "fr.insee.rmes.bauhaus.concepts.defaultContributor=stamp",
        "fr.insee.rmes.bauhaus.sugoi.ui=sugoUi",
        "fr.insee.rmes.bauhaus.appHost=http://localhost",
        "fr.insee.rmes.bauhaus.activeModules=operations,concepts",
        "fr.insee.rmes.bauhaus.modules=operations,concepts"
})
class PublicResourcesTest {
    @Autowired
    PublicResources publicResources;

    @Test
    void shouldReturnTheInitPayload() throws RmesException {
        String body = (String) publicResources.getProperties().getBody();
        assert body != null;
        JSONObject properties = new JSONObject(body);
        Assertions.assertEquals(properties.getString("appHost"), "http://localhost");
        Assertions.assertEquals(properties.getString("authorizationHost"), "sugoUi");
        Assertions.assertEquals(properties.getString("defaultContributor"), "stamp");
        Assertions.assertEquals(properties.getString("defaultMailSender"), "email");
        Assertions.assertEquals(properties.getString("maxLengthScopeNote"), "35");
        Assertions.assertEquals(properties.getString("lg1"), "fr");
        Assertions.assertEquals(properties.getString("lg2"), "en");
        Assertions.assertEquals(properties.getString("authType"), "NoAuthImpl");
        Assertions.assertEquals(properties.getJSONArray("activeModules").get(0), "operations");
        Assertions.assertEquals(properties.getJSONArray("activeModules").get(1), "concepts");
        Assertions.assertEquals(properties.getJSONArray("modules").get(0), "operations");
        Assertions.assertEquals(properties.getJSONArray("modules").get(1), "concepts");
    }
}