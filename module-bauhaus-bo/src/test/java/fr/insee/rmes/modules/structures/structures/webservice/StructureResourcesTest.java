package fr.insee.rmes.modules.structures.structures.webservice;

import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.structures.structures.domain.model.PartialStructure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StructureResourcesTest {

    @Mock
    private StructureService structureService;

    @Mock
    private StructureComponent structureComponentService;

    private StructureResources structureResources;

    @BeforeEach
    void setUp() {
        structureResources = new StructureResources(structureService, structureComponentService);
    }

    @Test
    void shouldReturn200WhenFetchingStructureById() throws RmesException {
        when(structureService.getStructureById(anyString())).thenReturn("result");
        ResponseEntity<?> response = structureResources.getStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn200WhenPublishingAStructure() throws RmesException {
        when(structureService.publishStructureById(anyString())).thenReturn("result publishing");
        ResponseEntity<?> response = structureResources.publishStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result publishing", response.getBody());
    }

    @Test
    void shouldGetStructures() throws RmesException {
        // Given
        PartialStructure structure1 = new PartialStructure(
                "http://example.com/structure1", "s1", "Label 1", "creator1", "Validated"
        );
        PartialStructure structure2 = new PartialStructure(
                "http://example.com/structure2", "s2", "Label 2", "creator2", "Unpublished"
        );
        List<PartialStructure> expectedStructures = Arrays.asList(structure1, structure2);
        when(structureService.getStructures()).thenReturn(expectedStructures);

        // When
        ResponseEntity<List<PartialStructureResponse>> result = structureResources.getStructures();

        // Then
        Assertions.assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        Assertions.assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        verify(structureService, times(1)).getStructures();
    }

    @Test
    void should_get_structures_with_hateoas_links() throws RmesException {
        // Given
        PartialStructure structure1 = new PartialStructure(
                "http://example.com/structure1", "s1", "Label 1", "creator1", "Validated"
        );
        List<PartialStructure> expectedStructures = List.of(structure1);
        when(structureService.getStructures()).thenReturn(expectedStructures);

        // When
        ResponseEntity<List<PartialStructureResponse>> result = structureResources.getStructures();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());

        PartialStructureResponse structureResponse = result.getBody().get(0);
        Assertions.assertTrue(structureResponse.hasLinks());
        Assertions.assertTrue(structureResponse.getLink("self").isPresent());

        // Verify the self link URL
        String selfLink = structureResponse.getLink("self").get().getHref();
        Assertions.assertTrue(selfLink.contains("/structures/structure/s1"),
                "Self link should contain '/structures/structure/s1' but was: " + selfLink);

        verify(structureService, times(1)).getStructures();
    }

    @Test
    void should_return_empty_list_when_no_structures() throws RmesException {
        // Given
        when(structureService.getStructures()).thenReturn(List.of());

        // When
        ResponseEntity<List<PartialStructureResponse>> result = structureResources.getStructures();

        // Then
        Assertions.assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        Assertions.assertNotNull(result.getBody());
        assertEquals(0, result.getBody().size());
        verify(structureService, times(1)).getStructures();
    }
}