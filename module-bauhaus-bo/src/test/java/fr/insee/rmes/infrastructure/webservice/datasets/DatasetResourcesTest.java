package fr.insee.rmes.infrastructure.webservice.datasets;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/infrastructure/webservice/datasets/DatasetResourcesTest.java
=======
import fr.insee.rmes.onion.infrastructure.webservice.datasets.DatasetResources;
>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/test/java/fr/insee/rmes/infrastructure/webservice/datasets/DatasetResourcesTest.java
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class DatasetResourcesTest {

    @MockitoBean
    ConceptsService conceptsService;

    @MockitoBean
    DatasetService datasetService;

    @Test
    void shouldReturnResponseWhenDeleteDataset() throws RmesException {
        doNothing().when(conceptsService).deleteConcept("id mocked");
        DatasetResources datasetResources= new DatasetResources(datasetService);
        String actual = datasetResources.deleteDataset("id mocked").toString();
        Assertions.assertEquals("<200 OK OK,[]>",actual);
    }

}