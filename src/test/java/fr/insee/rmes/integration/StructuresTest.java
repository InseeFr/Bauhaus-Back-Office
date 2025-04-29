package fr.insee.rmes.integration;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.structures.impl.StructureComponentImpl;
import fr.insee.rmes.bauhaus_services.structures.impl.StructureImpl;
import fr.insee.rmes.bauhaus_services.structures.utils.StructureComponentUtils;
import fr.insee.rmes.bauhaus_services.structures.utils.StructureUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.webservice.structures.StructureResources;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(controllers = StructureResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=structures",
                "fr.insee.rmes.bauhaus.baseGraph=http://",
                "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://",
                "fr.insee.rmes.bauhaus.datasets.graph=datasetGraph/",
                "fr.insee.rmes.bauhaus.datasets.baseURI=datasetIRI",
                "fr.insee.rmes.bauhaus.datasets.record.baseURI=recordIRI",
                "fr.insee.rmes.bauhaus.distribution.baseURI=distributionIRI",
                "fr.insee.rmes.bauhaus.adms.graph=adms",
                "fr.insee.rmes.bauhaus.adms.identifiantsAlternatifs.baseURI=identifiantsAlternatifs/jeuDeDonnees",
                "fr.insee.rmes.bauhaus.lg1=fr",
                "fr.insee.rmes.bauhaus.lg2=en"}
)
@Import({
        StructureComponentImpl.class,
        StructureImpl.class})
class StructuresTest extends AbstractResourcesEnvProd{

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";
    @MockitoBean
    RepositoryGestion repositoryGestion;
    @MockitoBean
    StructureComponentUtils structureComponentUtils;
    @MockitoBean
    protected IdGenerator idGenerator;
    @MockitoBean
    protected RepositoryPublication repositoryPublication;
    @MockitoBean
    protected PublicationUtils publicationUtils;
    @MockitoBean
    protected OperationsDocumentationsService documentationsService;
    @MockitoBean
    StructureUtils structureUtils;
    @MockitoBean
    CodeListService codeListService;
    @Autowired
    private MockMvc mvc;

    @Test
    void getComponentAsNoRole_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        String jsonSource = """
                [
                {"iri":"monIri", "id":"1"},
                {"iri":"monIri4", "id":"4", "labelLg1": "B"},
                {"iri":"monIri2", "id":"2"},
                {"iri":"monIri3", "id":"3", "labelLg1": "A"},
                ]
                """;
        String expectedJson = """
                [{"iri":"monIri","id":"1","identifiant":null,"labelLg1":null,"concept":null,"type":null,"codeList":null,"validationState":null,"creator":null,"range":null},{"iri":"monIri2","id":"2","identifiant":null,"labelLg1":null,"concept":null,"type":null,"codeList":null,"validationState":null,"creator":null,"range":null},{"iri":"monIri3","id":"3","identifiant":null,"labelLg1":"A","concept":null,"type":null,"codeList":null,"validationState":null,"creator":null,"range":null},{"iri":"monIri4","id":"4","identifiant":null,"labelLg1":"B","concept":null,"type":null,"codeList":null,"validationState":null,"creator":null,"range":null}]""";
        JSONArray resultArray = new JSONArray(jsonSource);
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(resultArray);


        mvc.perform(get("/structures/components/").header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(expectedJson));
    }

    @Test
    void getAllStructuresAsNoRole_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        String jsonSource = """
                [
                {"iri":"monIri", "id":"1"},
                {"iri":"monIri4", "id":"4", "labelLg1": "B"},
                {"iri":"monIri2", "id":"2"},
                {"iri":"monIri3", "id":"3", "labelLg1": "A"},
                ]
                """;
        String expectedJson = """
                [{"iri":"monIri","id":"1","labelLg1":null,"creator":null,"validationState":null},{"iri":"monIri2","id":"2","labelLg1":null,"creator":null,"validationState":null},{"iri":"monIri3","id":"3","labelLg1":"A","creator":null,"validationState":null},{"iri":"monIri4","id":"4","labelLg1":"B","creator":null,"validationState":null}]""";
        JSONArray resultArray = new JSONArray(jsonSource);
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(resultArray);
        mvc.perform(get("/structures")
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(expectedJson));
    }

}
