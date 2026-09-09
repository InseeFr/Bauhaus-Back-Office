package fr.insee.rmes.testcontainers.documentations;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.shared_kernel.domain.model.Roles;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.clientside.AccessPrivilegesCheckerService;
import fr.insee.rmes.modules.operations.msd.infrastructure.graphdb.GraphDBDocumentationRepository;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Filet de sécurité E2E : l'ordre des documents d'une rubrique RICHTEXT, stocké
 * sous forme de rdf:List (insee:additionalMaterial -> rdf:first/rdf:rest), doit
 * être préservé lors de la PUBLICATION (recopie gestion -> publication).
 *
 * La publication recopie le graphe du SIMS triplet par triplet
 * (DocumentationPublication#publishSims) : ce test verrouille le fait que les
 * maillons de la liste (noeuds blancs + rdf:first/rdf:rest) sont bien transférés
 * dans le graphe de publication, et dans le bon ordre. Il garde le système
 * honnête si quelqu'un excluait additionalMaterial de la publication ou modifiait
 * transformTripleToPublish.
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
                "fr.insee.rmes.bauhaus.modules.operations.enabled=true",
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
class SimsDocumentOrderPublicationEndToEndTest extends WithGraphDBContainer {

    private static final String PUBLICATION_GRAPH = "http://rdf.insee.fr/graphes/qualite/rapport/7777";
    private static final String PUBLISHED_TEXT_URI = "http://publication/qualite/attribut/7777/S.3.1/texte";

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
        container.withTrigFiles("sims-document-order-publication.trig");
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
    @DisplayName("La publication d'un SIMS conserve l'ordre de la liste de documents")
    void publishingASimsKeepsDocumentListOrder() throws Exception, MissingUserInformationException {
        configureJwtDecoderMock(jwtDecoder, "admin", "XX59-YYY", List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer test-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/metadataReport/7777/validate",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        // Reconstitue l'ordre de la rdf:List dans le graphe de publication : la
        // position d'un maillon = nombre de maillons depuis la tête jusqu'à lui.
        // IRI complètes (le repository préfixe déjà la requête avec rdf:, insee:...).
        String orderedListQuery = """
                SELECT ?doc (COUNT(?mid) AS ?pos) WHERE {
                    GRAPH <%s> {
                        <%s> <http://rdf.insee.fr/def/base#additionalMaterial> ?head .
                        ?head <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>* ?member .
                        ?member <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?doc .
                        ?head <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>* ?mid .
                        ?mid <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>* ?member .
                    }
                }
                GROUP BY ?doc
                ORDER BY ?pos
                """.formatted(PUBLICATION_GRAPH, PUBLISHED_TEXT_URI);

        JSONArray published = repositoryPublication.getResponseAsArray(orderedListQuery);

        List<String> publishedDocs = new ArrayList<>(published.length());
        JSONUtils.stream(published)
                .map(row -> row.getString("doc"))
                .forEach(publishedDocs::add);

        assertThat(publishedDocs)
                .as("Les documents doivent être publiés dans le graphe de publication, "
                        + "avec leur IRI de publication, et dans l'ordre de la liste RDF")
                .containsExactly(
                        "http://publication/documents/document/801",
                        "http://publication/documents/document/802",
                        "http://publication/documents/document/803");
    }
}
