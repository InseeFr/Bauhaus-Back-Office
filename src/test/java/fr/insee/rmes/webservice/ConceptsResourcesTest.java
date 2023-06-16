package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.WebServiceConfiguration;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.http.HttpStatus;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static fr.insee.rmes.webservice.DocumentsResourcesTest.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(properties={"fr.insee.rmes.bauhaus.env=PROD", STAMP_CLAIM_PROPERTY, ROLE_CLAIM_PROPERTY, ID_CLAIM_PROPERTY},
        controllers = ConceptsResources.class)
@AutoConfigureMockMvc
@Import({AuthorizeMethodDecider.class, Config.class, WebServiceConfiguration.class})
class ConceptsResourcesTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ConceptsService conceptsService;

    @MockBean
    private ConceptsCollectionService conceptsCollectionService;

    @BeforeEach
    public void init() {
        Date issuedAt=new Date();
        Date expiresAT=Date.from((new Date()).toInstant().plusSeconds(100));
        Jwt jwt=new Jwt("token",issuedAt.toInstant(),expiresAT.toInstant(),
                Map.of("alg", "RS256","typ", "JWT"),
                Map.of("realm_access", "{\"roles\":[]}",
                        "timbre", "DR59-SNDI",
                        "preferred_username", "bibi"
                )
        );
        when(this.jwtDecoder.decode(anyString())).thenReturn(jwt);
    }

    @Test
    void shouldReturn500IfRmesExceptionWhenFetchingCollectionById(@Autowired ConceptsResources conceptsResources) throws RmesException {
        when(conceptsCollectionService.getCollectionByID(anyString())).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        ResponseEntity<?> response = conceptsResources.getCollectionByID("1");
        Assertions.assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void shouldReturn200WhenFetchingCollectionById(@Autowired ConceptsResources conceptsResources) throws RmesException {
        when(conceptsCollectionService.getCollectionByID(anyString())).thenReturn("result");
        ResponseEntity<?> response = conceptsResources.getCollectionByID("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void getConcepts_shouldReturnEtagHeader(@Autowired MockMvc mvc) throws Exception {
        String response="{\"bigJson\":true}";

        when(conceptsService.getConcepts()).thenReturn(response);

        PersoMatcher matcher=PersoMatcher.checkNotEmptyAndStoreEtag();


        mvc.perform(get("/concepts").header("Authorization", "bearer token_blabla"))
                .andExpect(status().isOk())
                .andExpect(header().string("cache-control",containsString("no-cache")))
                .andExpect(header().string("etag", matcher))
                .andExpect(content().string(response));

        mvc.perform(get("/concepts")
                        .header("Authorization", "bearer token_blabla")
                        .header("If-None-Match", matcher.etag())
                )
                .andExpect(status().isNotModified())
                .andExpect(header().string("cache-control",containsString("no-cache")))
                .andExpect(header().string("etag", not(emptyOrNullString())))
                .andExpect(content().string(emptyOrNullString()));
    }

    @Test
    void getRelatedConcepts_shouldUseCache(@Autowired MockMvc mvc) throws Exception {
        var id = "1";
        String response="{\"bigJson\":true}";
        when(conceptsService.getRelatedConcepts(id)).thenReturn(response);
        PersoMatcher matcher=PersoMatcher.checkNotEmptyAndStoreEtag();

        mvc.perform(get("/concepts/linkedConcepts/"+id).header("Authorization", "bearer token_blabla"))
                .andExpect(status().isOk())
                .andExpect(header().string("cache-control",containsString("no-cache")))
                .andExpect(header().string("cache-control",not(containsString("no-store"))))
                .andExpect(header().string("etag", matcher))
                .andExpect(content().string(response));
        mvc.perform(get("/concepts/linkedConcepts/"+id)
                        .header("Authorization", "bearer token_blabla")
                        .header("If-None-Match", matcher.etag())
                )
                .andExpect(status().isNotModified())
                .andExpect(header().string("cache-control",containsString("no-cache")))
                .andExpect(header().string("cache-control",not(containsString("no-store"))))
                .andExpect(header().string("etag", not(emptyOrNullString())))
                .andExpect(content().string(emptyOrNullString()));
    }

    private static class PersoMatcher extends BaseMatcher<String>{

        private final BiPredicate<Object, Consumer<String>> matcher;
        private String etag;

        private PersoMatcher(BiPredicate<Object, Consumer<String>> matcher) {
            this.matcher = matcher;
        }

        public static PersoMatcher print() {
            return new PersoMatcher((actual, consumer)->{
                System.out.println(actual);
                return true;
            });
        }

        public static PersoMatcher checkNotEmptyAndStoreEtag() {
            return new PersoMatcher((actual, consumer)->{
                boolean isNotNullNotEmpty=actual instanceof String && ((String)actual).length()>0;
                if (isNotNullNotEmpty){
                    consumer.accept((String)actual);
                }
                return isNotNullNotEmpty;
            }
            );
        }


        @Override
        public void describeTo(Description description) {

        }

        @Override
        public boolean matches(Object actual) {
            return matcher.test(actual, this::setEtag);
        }

        private void setEtag(String s) {
            this.etag=s;
        }

        public String etag() {
            return this.etag;
        }
    }
}
