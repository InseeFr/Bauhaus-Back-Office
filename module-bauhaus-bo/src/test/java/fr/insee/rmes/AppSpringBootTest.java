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
                // Seul 'operations' reste actif : les controleurs des autres modules ne sont pas
                // charges, ce qui garde le contexte de test leger.
                "fr.insee.rmes.bauhaus.modules.concepts.enabled=false",
                "fr.insee.rmes.bauhaus.modules.classifications.enabled=false",
                "fr.insee.rmes.bauhaus.modules.structures.enabled=false",
                "fr.insee.rmes.bauhaus.modules.codelists.enabled=false",
                "fr.insee.rmes.bauhaus.modules.datasets.enabled=false",
                "fr.insee.rmes.bauhaus.modules.ddi.enabled=false",
                "spring.hateoas.use-hal-as-default-json-media-type=true"
        }
)
@AutoConfigureTestRestTemplate
@Import(GraphDBDocumentationRepository.class)
public @interface AppSpringBootTest {}