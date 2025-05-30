package fr.insee.rmes.webservice;

import fr.insee.rmes.webservice.classifications.ClassificationsResources;
import fr.insee.rmes.webservice.concepts.ConceptsCollectionsResources;
import fr.insee.rmes.webservice.concepts.ConceptsResources;
import fr.insee.rmes.webservice.operations.*;
import fr.insee.rmes.webservice.structures.StructureResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(properties = "fr.insee.rmes.bauhaus.activeModules=")
@ExtendWith(MockitoExtension.class)
class UnactiveModulesTest {


    @Autowired(required = false)
    ClassificationsResources classificationsResources;

    @Autowired(required = false)
    ConceptsCollectionsResources conceptsCollectionsResources;

    @Autowired(required = false)
    ConceptsResources conceptsResources;

    @Autowired(required = false)
    StructureResources structureResources;

    @Autowired(required = false)
    DocumentsResources documentsResources;

    @Autowired(required = false)
    FamilyResources familyResources;

    @Autowired(required = false)
    IndicatorsResources indicatorsResources;

    @Autowired(required = false)
    MetadataReportResources metadataReportResources;

    @Autowired(required = false)
    OperationsResources operationsResources;

    @Autowired(required = false)
    SeriesResources seriesResources;

    @Test
    void shouldReturnAnErrorIfTheClassificationsModuleIsNotActive(){
        Assertions.assertNull(classificationsResources);
    }

    @Test
    void shouldReturnAnErrorIfTheConceptsModuleIsNotActive(){
        Assertions.assertNull(conceptsCollectionsResources);
        Assertions.assertNull(conceptsResources);
    }

    @Test
    void shouldReturnAnErrorIfTheStructuresModuleIsNotActive(){
        Assertions.assertNull(structureResources);
    }

    @Test
    void shouldReturnAnErrorIfTheOperationsModuleIsNotActive(){
        Assertions.assertNull(documentsResources);
        Assertions.assertNull(familyResources);
        Assertions.assertNull(indicatorsResources);
        Assertions.assertNull(metadataReportResources);
        Assertions.assertNull(operationsResources);
        Assertions.assertNull(seriesResources);
    }

}