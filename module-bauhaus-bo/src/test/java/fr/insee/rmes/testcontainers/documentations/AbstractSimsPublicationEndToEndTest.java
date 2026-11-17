package fr.insee.rmes.testcontainers.documentations;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.modules.operations.msd.infrastructure.graphdb.GraphDBDocumentationRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.Roles;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.clientside.AccessPrivilegesCheckerService;
import fr.insee.rmes.testcontainers.GraphDbTestProperties;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.util.List;
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

/**
 * Socle des tests E2E de publication d'un SIMS : application complète sur GraphDB, appel HTTP de
 * validation d'un rapport qualité par un administrateur, puis lecture du graphe de publication.
 */
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
        })
@AutoConfigureTestRestTemplate
@Import(GraphDBDocumentationRepository.class)
abstract class AbstractSimsPublicationEndToEndTest extends WithGraphDBContainer {

    @DynamicPropertySource
    static void configureGraphDB(DynamicPropertyRegistry registry) {
        GraphDbTestProperties.registerGestionAndSharedPublication(registry);
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

    @MockitoBean(name = "propertiesAccessPrivilegesChecker")
    AccessPrivilegesCheckerService checker;

    /** Valide (publie) le rapport qualité {@code simsId} en tant qu'administrateur. */
    ResponseEntity<String> validateSimsAsAdmin(String simsId) throws MissingUserInformationException {
        configureJwtDecoderMock(jwtDecoder, "admin", "XX59-YYY", List.of(Roles.ADMIN));
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer test-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/metadataReport/" + simsId + "/validate",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class);
    }
}
