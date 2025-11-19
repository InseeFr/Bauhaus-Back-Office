package fr.insee.rmes.modules.operations.msd.webservice;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.operations.DocumentationAttribute;
import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.OperationDocumentationRubricWithoutRangeException;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class MetadataReportResourcesTest {

    @MockitoBean
    OperationsService operationsService;

    @MockitoBean
    OperationsDocumentationsService documentationsService;

    @MockitoBean
    DocumentationService documentationService;

    @Test
    void shouldReturnMetadataAttributesWithHateoasLinks() throws RmesException, GenericInternalServerException, OperationDocumentationRubricWithoutRangeException {
        MetadataReportResources metadataReportResources = new MetadataReportResources(operationsService, documentationsService, documentationService);

        DocumentationAttribute attr1 = new DocumentationAttribute("text", "Label 1", "Label 1 EN", "attr-1", "1", false, false, null);
        DocumentationAttribute attr2 = new DocumentationAttribute("richText", "Label 2", "Label 2 EN", "attr-2", "n", true, false, "codeList1");
        List<DocumentationAttribute> attributes = List.of(attr1, attr2);

        when(documentationService.getMetadataAttributes()).thenReturn(attributes);

        var response = metadataReportResources.getMetadataAttributes();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(2, response.getBody().size());

        // Verify HATEOAS links are present
        var firstAttribute = response.getBody().get(0);
        Assertions.assertTrue(firstAttribute.hasLinks(), "First attribute should have links");
        Assertions.assertTrue(firstAttribute.hasLink("self"), "First attribute should have self link");
        Assertions.assertTrue(firstAttribute.getLink("self").get().getHref().contains("/operations/metadataAttribute/attr-1"));

        var secondAttribute = response.getBody().get(1);
        Assertions.assertTrue(secondAttribute.hasLinks(), "Second attribute should have links");
        Assertions.assertTrue(secondAttribute.hasLink("self"), "Second attribute should have self link");
        Assertions.assertTrue(secondAttribute.getLink("self").get().getHref().contains("/operations/metadataAttribute/attr-2"));
    }
}
