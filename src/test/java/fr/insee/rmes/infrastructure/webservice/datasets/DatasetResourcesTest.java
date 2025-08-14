package fr.insee.rmes.infrastructure.webservice.datasets;

import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
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