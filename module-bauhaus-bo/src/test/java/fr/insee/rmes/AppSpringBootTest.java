package fr.insee.rmes;

import fr.insee.rmes.onion.infrastructure.graphdb.operations.GraphDBDocumentationRepository;
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
                "fr.insee.rmes.bauhaus.baseGraph=http://rdf.insee.fr/graphes/",
                "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://",
                "fr.insee.rmes.bauhaus.datasets.graph=datasetGraph/",
                "fr.insee.rmes.bauhaus.datasets.baseURI=datasetIRI",
                "fr.insee.rmes.bauhaus.datasets.record.baseURI=recordIRI",
                "fr.insee.rmes.bauhaus.distribution.baseURI=distributionIRI",
                "fr.insee.rmes.bauhaus.adms.graph=adms",
                "fr.insee.rmes.bauhaus.adms.identifiantsAlternatifs.baseURI=identifiantsAlternatifs/jeuDeDonnees",
                "fr.insee.rmes.bauhaus.lg1=fr",
                "fr.insee.rmes.bauhaus.lg2=en",
                "fr.insee.rmes.bauhaus.modules[0].identifier=concepts",
                "fr.insee.rmes.bauhaus.modules[0].disabled=true",
                "fr.insee.rmes.bauhaus.modules[1].identifier=classifications",
                "fr.insee.rmes.bauhaus.modules[1].disabled=true",
                "fr.insee.rmes.bauhaus.modules[2].identifier=operations",
                "fr.insee.rmes.bauhaus.modules[3].identifier=structures",
                "fr.insee.rmes.bauhaus.modules[3].disabled=true",
                "fr.insee.rmes.bauhaus.modules[4].identifier=codelists",
                "fr.insee.rmes.bauhaus.modules[4].disabled=true",
                "fr.insee.rmes.bauhaus.modules[5].identifier=datasets",
                "fr.insee.rmes.bauhaus.modules[5].disabled=true",
                "fr.insee.rmes.bauhaus.modules[6].identifier=ddi",
                "fr.insee.rmes.bauhaus.modules[6].disabled=true",
                "fr.insee.rmes.bauhaus.operations.graph=operations",
                "spring.hateoas.use-hal-as-default-json-media-type=true"
        }
)
@Import(GraphDBDocumentationRepository.class)
public @interface AppSpringBootTest {}