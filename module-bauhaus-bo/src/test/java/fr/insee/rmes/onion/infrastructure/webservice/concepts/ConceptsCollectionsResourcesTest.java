package fr.insee.rmes.onion.infrastructure.webservice.concepts;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.Language;
import fr.insee.rmes.model.concepts.PartialCollection;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConceptsCollectionsResourcesTest {

    @Mock
    private ConceptsService conceptsService;

    @Mock
    private ConceptsCollectionService conceptsCollectionService;

    @Mock
    private HttpServletResponse response;

    private ConceptsCollectionsResources resources;

    @BeforeEach
    void setUp() {
        resources = new ConceptsCollectionsResources(conceptsService, conceptsCollectionService);
    }

    @Test
    void shouldGetCollections() throws RmesException {
        // Given
        List<PartialCollection> expectedCollections = List.of(
                new PartialCollection("1", "Collection 1"),
                new PartialCollection("2", "Collection 2")
        );
        when(conceptsCollectionService.getCollections()).thenReturn(expectedCollections);

        // When
        List<PartialCollection> result = resources.getCollections();

        // Then
        assertEquals(expectedCollections, result);
        verify(conceptsCollectionService, times(1)).getCollections();
    }

    @Test
    void shouldGetCollectionExport() throws RmesException {
        // Given
        String id = "c1";
        String accept = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        ResponseEntity<?> expectedResponse = ResponseEntity.ok().build();
        when(conceptsService.getCollectionExport(id, accept)).thenReturn((ResponseEntity) expectedResponse);

        // When
        ResponseEntity<?> result = resources.getCollectionExport(id, accept);

        // Then
        assertEquals(expectedResponse, result);
        verify(conceptsService, times(1)).getCollectionExport(id, accept);
    }

    @Test
    void shouldExportZipCollection() throws RmesException {
        // Given
        String id = "c1";
        String type = "odt";
        Language lg = Language.lg1;
        String accept = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        boolean withConcepts = true;

        // When
        resources.exportZipCollection(id, type, lg, accept, withConcepts, response);

        // Then
        verify(conceptsCollectionService, times(1))
                .exportZipCollection(id, accept, response, lg, type, withConcepts);
    }

    @Test
    void shouldGetCollectionExportWithType_Ods() throws RmesException {
        // Given
        String id = "c1";
        String type = "ods";
        Language lg = Language.lg1;
        boolean withConcepts = false;
        String accept = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        ResponseEntity<?> expectedResponse = ResponseEntity.ok().build();

        when(conceptsCollectionService.getCollectionExportODS(id, accept, withConcepts, response))
                .thenReturn((ResponseEntity) expectedResponse);

        // When
        ResponseEntity<?> result = resources.getCollectionExport(id, type, lg, withConcepts, accept, response);

        // Then
        assertEquals(expectedResponse, result);
        verify(conceptsCollectionService, times(1))
                .getCollectionExportODS(id, accept, withConcepts, response);
        verify(conceptsCollectionService, never())
                .getCollectionExportODT(anyString(), anyString(), any(), anyBoolean(), any());
    }

    @Test
    void shouldGetCollectionExportWithType_Odt() throws RmesException {
        // Given
        String id = "c1";
        String type = "odt";
        Language lg = Language.lg2;
        boolean withConcepts = true;
        String accept = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        ResponseEntity<?> expectedResponse = ResponseEntity.ok().build();

        when(conceptsCollectionService.getCollectionExportODT(id, accept, lg, withConcepts, response))
                .thenReturn((ResponseEntity) expectedResponse);

        // When
        ResponseEntity<?> result = resources.getCollectionExport(id, type, lg, withConcepts, accept, response);

        // Then
        assertEquals(expectedResponse, result);
        verify(conceptsCollectionService, times(1))
                .getCollectionExportODT(id, accept, lg, withConcepts, response);
        verify(conceptsCollectionService, never())
                .getCollectionExportODS(anyString(), anyString(), anyBoolean(), any());
    }

    @Test
    void shouldGetCollectionExportWithType_OdsCaseInsensitive() throws RmesException {
        // Given
        String id = "c1";
        String type = "ODS"; // Test case insensitivity
        Language lg = Language.lg1;
        boolean withConcepts = false;
        String accept = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        ResponseEntity<?> expectedResponse = ResponseEntity.ok().build();

        when(conceptsCollectionService.getCollectionExportODS(id, accept, withConcepts, response))
                .thenReturn((ResponseEntity) expectedResponse);

        // When
        ResponseEntity<?> result = resources.getCollectionExport(id, type, lg, withConcepts, accept, response);

        // Then
        assertEquals(expectedResponse, result);
        verify(conceptsCollectionService, times(1))
                .getCollectionExportODS(id, accept, withConcepts, response);
    }

    @Test
    void shouldGetCollectionExportWithType_AnyOtherType() throws RmesException {
        // Given
        String id = "c1";
        String type = "pdf"; // Any type other than "ods"
        Language lg = Language.lg1;
        boolean withConcepts = true;
        String accept = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        ResponseEntity<?> expectedResponse = ResponseEntity.ok().build();

        when(conceptsCollectionService.getCollectionExportODT(id, accept, lg, withConcepts, response))
                .thenReturn((ResponseEntity) expectedResponse);

        // When
        ResponseEntity<?> result = resources.getCollectionExport(id, type, lg, withConcepts, accept, response);

        // Then
        assertEquals(expectedResponse, result);
        verify(conceptsCollectionService, times(1))
                .getCollectionExportODT(id, accept, lg, withConcepts, response);
    }
}
