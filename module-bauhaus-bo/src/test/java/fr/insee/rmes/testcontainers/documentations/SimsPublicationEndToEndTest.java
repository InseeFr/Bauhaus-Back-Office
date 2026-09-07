package fr.insee.rmes.testcontainers.documentations;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.modules.shared_kernel.domain.model.Roles;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.clientside.AccessPrivilegesCheckerService;
import fr.insee.rmes.modules.operations.msd.infrastructure.graphdb.GraphDBDocumentationRepository;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test E2E reproduisant le bug : insee:html de la rubrique S.3.1 (RICHTEXT)
 * est stocké dans le graphe rapport/null au lieu du graphe rapport/8888.
 *
 * Cause : addHtmlVersionForRichText() utilise RdfUtils.simsGraph(null) comme
 * contexte du graphe au lieu de RdfUtils.simsGraph(simsId).
 *
 * Ce test ÉCHOUE intentionnellement tant que le bug n'est pas corrigé.
 */
@Tag("integration")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.config.additional-location=classpath:testing-rbac.yml",
                // baseGraph, adms.graph, adms.identifiantsAlternatifs.baseURI, lg1, lg2 and
                // operations.graph are omitted: identical to the main config chain values.
                "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://bauhaus/",
                "fr.insee.rmes.bauhaus.sesame.publication.baseURI=http://publication/",
                "fr.insee.rmes.bauhaus.datasets.graph=datasetGraph/",
                "fr.insee.rmes.bauhaus.datasets.baseURI=datasetIRI",
                "fr.insee.rmes.bauhaus.datasets.record.baseURI=recordIRI",
                "fr.insee.rmes.bauhaus.distribution.baseURI=distributionIRI",
                "fr.insee.rmes.bauhaus.modules[0].identifier=operations",
                "fr.insee.rmes.bauhaus.documentation.geographie.baseUri=http://bauhaus/qualite/territoire/",
                "fr.insee.rmes.bauhaus.documentation.titlePrefixLg1=Rapport qualité :",
                "fr.insee.rmes.bauhaus.documentation.titlePrefixLg2=Quality report:",
                "fr.insee.rmes.bauhaus.organisations.graph=http://rdf.insee.fr/graphes/organisations",
                "fr.insee.rmes.bauhaus.cors.allowedOrigin=*",
                "jwt.idClaim=preferred_username",
                "jwt.stampClaim=timbre",
                "jwt.roleClaim=realm_access",
                "jwt.roleClaimConfig.roles=roles",
                "spring.hateoas.use-hal-as-default-json-media-type=true"
        }
)
@AutoConfigureTestRestTemplate
@Import(GraphDBDocumentationRepository.class)
class SimsPublicationEndToEndTest extends WithGraphDBContainer {

    @DynamicPropertySource
    static void configureGraphDB(DynamicPropertyRegistry registry) {
        String graphdbUrl = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> graphdbUrl);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.sesameServer", () -> graphdbUrl);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.repository", () -> BAUHAUS_TEST_REPOSITORY);
    }

    @BeforeAll
    static void initData() {
        container.withTrigFiles("sims-s31-richtext-publication-test.trig");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    RepositoryPublication repositoryPublication;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    DocumentsPublication documentsPublication;

    @MockitoBean
    DocumentsService documentsService;

    @MockitoBean(name = "propertiesAccessPrivilegesChecker")
    AccessPrivilegesCheckerService checker;

    @Test
    void validate_htmlS31DevraitEtreDansLeGrapheRapport8888() throws Exception, MissingUserInformationException {
        configureJwtDecoderMock(jwtDecoder, "admin", "XX59-YYY", List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer test-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/metadataReport/8888/validate",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        // BUG : addHtmlVersionForRichText() utilise simsGraph(null) → graphe rapport/null
        // au lieu de simsGraph("8888") → graphe rapport/8888

        String queryGrapheCorrect = """
                SELECT ?html WHERE {
                    GRAPH <http://rdf.insee.fr/graphes/qualite/rapport/8888> {
                        ?textResource <http://rdf.insee.fr/def/base#html> ?html .
                    }
                }
                """;


        var dansGrapheCorrect = repositoryPublication.getResponseAsArray(queryGrapheCorrect);


        assertThat(dansGrapheCorrect)
                .as("insee:html de S.3.1 devrait être dans le graphe rapport/8888, pas dans rapport/null")
                .hasSizeGreaterThan(0);
    }
}
