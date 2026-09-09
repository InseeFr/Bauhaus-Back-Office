package fr.insee.rmes;

import fr.insee.rmes.modules.operations.msd.infrastructure.graphdb.GraphDBDocumentationRepository;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.config.additional-location=classpath:testing-rbac.yml",
                // baseGraph, adms.graph, adms.identifiantsAlternatifs.baseURI, lg1, lg2 and
                // operations.graph are intentionally omitted: their test values are identical to the
                // ones already provided by the main config chain (bauhaus-core.properties / bauhaus.yml).
                "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://",
                "fr.insee.rmes.bauhaus.datasets.graph=datasetGraph/",
                "fr.insee.rmes.bauhaus.datasets.baseURI=datasetIRI",
                "fr.insee.rmes.bauhaus.datasets.record.baseURI=recordIRI",
                "fr.insee.rmes.bauhaus.distribution.baseURI=distributionIRI",
                // La liste des modules declaree ici remplace entierement celle de bauhaus.yml :
                // seul 'operations' est actif, les controleurs des autres modules ne sont pas charges.
                "fr.insee.rmes.bauhaus.modules[0].identifier=operations",
                "spring.hateoas.use-hal-as-default-json-media-type=true"
        }
)
@AutoConfigureTestRestTemplate
@Import(GraphDBDocumentationRepository.class)
public @interface AppSpringBootTest {}