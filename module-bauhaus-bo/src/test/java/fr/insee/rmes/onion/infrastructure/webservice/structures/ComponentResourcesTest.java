package fr.insee.rmes.onion.infrastructure.webservice.structures;

import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.structures.components.webservice.ComponentResources;
import fr.insee.rmes.modules.structures.components.webservice.PartialStructureComponentResponse;
import fr.insee.rmes.modules.structures.structures.domain.model.PartialStructureComponent;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComponentResourcesTest {

    @Mock
    private StructureService structureService;

    @Mock
    private StructureComponent structureComponentService;

    private ComponentResources componentResources;

    @BeforeEach
    void setUp() {
        componentResources = new ComponentResources(structureService, structureComponentService);
    }

    @Test
    void shouldGetComponentsForSearch() throws RmesException {
        // Given
        String expectedComponents = "[{\"id\":\"c1\",\"label\":\"Component 1\"}]";
        when(structureComponentService.getComponentsForSearch()).thenReturn(expectedComponents);

        // When
        ResponseEntity<Object> result = componentResources.getComponentsForSearch();

        // Then
        assertEquals(HttpStatus.SC_OK, result.getStatusCode().value());
        assertEquals(expectedComponents, result.getBody());
        verify(structureComponentService, times(1)).getComponentsForSearch();
    }

    @Test
    void shouldGetAttributes() throws RmesException {
        // Given
        String expectedAttributes = "[{\"id\":\"a1\",\"label\":\"Attribute 1\"}]";
        when(structureComponentService.getAttributes()).thenReturn(expectedAttributes);

        // When
        ResponseEntity<Object> result = componentResources.getAttributes();

        // Then
        assertEquals(HttpStatus.SC_OK, result.getStatusCode().value());
        assertEquals(expectedAttributes, result.getBody());
        verify(structureComponentService, times(1)).getAttributes();
    }

    @Test
    void shouldGetComponents() throws RmesException {
        // Given
        PartialStructureComponent component1 = new PartialStructureComponent(
                "http://example.com/component1", "c1", "comp1", "Label 1",
                "concept1", "type1", "codeList1", "Validated", "creator1", "range1"
        );
        PartialStructureComponent component2 = new PartialStructureComponent(
                "http://example.com/component2", "c2", "comp2", "Label 2",
                "concept2", "type2", "codeList2", "Unpublished", "creator2", "range2"
        );
        List<PartialStructureComponent> expectedComponents = Arrays.asList(component1, component2);
        when(structureComponentService.getComponents()).thenReturn(expectedComponents);

        // When
        ResponseEntity<List<PartialStructureComponentResponse>> result = componentResources.getComponents();

        // Then
        Assertions.assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        Assertions.assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        verify(structureComponentService, times(1)).getComponents();
    }

    @Test
    void should_get_components_with_hateoas_links() throws RmesException {
        // Given
        PartialStructureComponent component1 = new PartialStructureComponent(
                "http://example.com/component1", "c1", "comp1", "Label 1",
                "concept1", "type1", "codeList1", "Validated", "creator1", "range1"
        );
        List<PartialStructureComponent> expectedComponents = List.of(component1);
        when(structureComponentService.getComponents()).thenReturn(expectedComponents);

        // When
        ResponseEntity<List<PartialStructureComponentResponse>> result = componentResources.getComponents();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());

        PartialStructureComponentResponse componentResponse = result.getBody().get(0);
        Assertions.assertTrue(componentResponse.hasLinks());
        Assertions.assertTrue(componentResponse.getLink("self").isPresent());
        verify(structureComponentService, times(1)).getComponents();
    }

    @Test
    void should_return_empty_list_when_no_components() throws RmesException {
        // Given
        when(structureComponentService.getComponents()).thenReturn(List.of());

        // When
        ResponseEntity<List<PartialStructureComponentResponse>> result = componentResources.getComponents();

        // Then
        Assertions.assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        Assertions.assertNotNull(result.getBody());
        assertEquals(0, result.getBody().size());
        verify(structureComponentService, times(1)).getComponents();
    }

    @Test
    void shouldGetComponentById() throws RmesException {
        // Given
        String id = "comp123";
        String expectedComponent = "{\"id\":\"comp123\",\"label\":\"Test Component\"}";
        when(structureComponentService.getComponent(id)).thenReturn(expectedComponent);

        // When
        ResponseEntity<Object> result = componentResources.getComponentById(id);

        // Then
        assertEquals(HttpStatus.SC_OK, result.getStatusCode().value());
        assertEquals(expectedComponent, result.getBody());
        verify(structureComponentService, times(1)).getComponent(id);
    }

    @Test
    void shouldPublishComponentById() throws RmesException {
        // Given
        String id = "comp123";
        String expectedResult = "{\"status\":\"published\"}";
        when(structureComponentService.publishComponent(id)).thenReturn(expectedResult);

        // When
        ResponseEntity<Object> result = componentResources.publishComponentById(id);

        // Then
        assertEquals(HttpStatus.SC_OK, result.getStatusCode().value());
        assertEquals(expectedResult, result.getBody());
        verify(structureComponentService, times(1)).publishComponent(id);
    }

    @Test
    void shouldDeleteComponentById() throws RmesException {
        // Given
        String id = "comp123";
        doNothing().when(structureComponentService).deleteComponent(id);

        // When
        ResponseEntity<Object> result = componentResources.deleteComponentById(id);

        // Then
        assertEquals(HttpStatus.SC_OK, result.getStatusCode().value());
        verify(structureComponentService, times(1)).deleteComponent(id);
    }

    @Test
    void shouldUpdateComponentById() throws RmesException {
        // Given
        String id = "comp123";
        String body = "{\"label\":\"Updated Component\"}";
        String expectedResult = "{\"id\":\"comp123\",\"label\":\"Updated Component\"}";
        when(structureComponentService.updateComponent(id, body)).thenReturn(expectedResult);

        // When
        ResponseEntity<Object> result = componentResources.updateComponentById(id, body);

        // Then
        assertEquals(HttpStatus.SC_OK, result.getStatusCode().value());
        assertEquals(expectedResult, result.getBody());
        verify(structureComponentService, times(1)).updateComponent(id, body);
    }

    @Test
    void shouldCreateComponent() throws RmesException {
        // Given
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/structures/components");
        req.setServerName("localhost");
        req.setServerPort(80);
        req.setScheme("http");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        String body = "{\"label\":\"New Component\"}";
        String expectedId = "comp456";
        when(structureComponentService.createComponent(body)).thenReturn(expectedId);

        // When
        ResponseEntity<Object> result = componentResources.createComponent(body);

        // Then
        assertEquals(HttpStatus.SC_CREATED, result.getStatusCode().value());
        assertEquals(expectedId, result.getBody());
        assertEquals(
                "/structures/components/" + expectedId,
                Objects.requireNonNull(result.getHeaders().getLocation()).getPath()
        );
        verify(structureComponentService, times(1)).createComponent(body);
    }
}