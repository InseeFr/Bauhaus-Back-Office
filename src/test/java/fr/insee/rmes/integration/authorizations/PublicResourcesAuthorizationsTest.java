package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.webservice.PublicResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext.PUBLIC_RESOURCES_ANT_PATTERNS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers= PublicResources.class )
class PublicResourcesAuthorizationsTest {

    @Autowired
    private MockMvc mvc;

    public static Stream<Arguments> endpointsProvider() {
        return Stream.of(PUBLIC_RESOURCES_ANT_PATTERNS).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("endpointsProvider")
    void ok_withoutAuth(String endpoint) throws Exception {
        this.mvc.perform(get(endpoint).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
