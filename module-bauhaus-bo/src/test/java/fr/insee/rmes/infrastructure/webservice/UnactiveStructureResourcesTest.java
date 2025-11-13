package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.modules.operations.documents.webservice.DocumentsResources;
import fr.insee.rmes.modules.operations.families.webservice.FamilyResources;
import fr.insee.rmes.modules.operations.indicators.webservice.IndicatorsResources;
import fr.insee.rmes.modules.operations.msd.webservice.MetadataReportResources;
import fr.insee.rmes.modules.operations.operations.webservice.OperationsResources;
import fr.insee.rmes.modules.operations.series.webservice.SeriesResources;
import fr.insee.rmes.modules.classifications.nomenclatures.webservice.ClassificationsResources;
import fr.insee.rmes.onion.infrastructure.webservice.concepts.ConceptsCollectionsResources;
import fr.insee.rmes.onion.infrastructure.webservice.concepts.ConceptsResources;
import fr.insee.rmes.modules.structures.structures.webservice.StructureResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;


@AppSpringBootTest
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
    void shouldNotReturnAnyErrorBecauseOperationsIsActive(){
        Assertions.assertNotNull(documentsResources);
        Assertions.assertNotNull(familyResources);
        Assertions.assertNotNull(indicatorsResources);
        Assertions.assertNotNull(metadataReportResources);
        Assertions.assertNotNull(operationsResources);
        Assertions.assertNotNull(seriesResources);
    }

}