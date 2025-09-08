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
        properties = {
                "spring.config.additional-location=classpath:rbac.yml",
                "fr.insee.rmes.bauhaus.baseGraph=http://",
                "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://",
                "fr.insee.rmes.bauhaus.datasets.graph=datasetGraph/",
                "fr.insee.rmes.bauhaus.datasets.baseURI=datasetIRI",
                "fr.insee.rmes.bauhaus.datasets.record.baseURI=recordIRI",
                "fr.insee.rmes.bauhaus.distribution.baseURI=distributionIRI",
                "fr.insee.rmes.bauhaus.adms.graph=adms",
                "fr.insee.rmes.bauhaus.adms.identifiantsAlternatifs.baseURI=identifiantsAlternatifs/jeuDeDonnees",
                "fr.insee.rmes.bauhaus.lg1=fr",
                "fr.insee.rmes.bauhaus.lg2=en",
                "fr.insee.rmes.bauhaus.activeModules="
        }
)
@Import(GraphDBDocumentationRepository.class)
public @interface AppSpringBootTest {}