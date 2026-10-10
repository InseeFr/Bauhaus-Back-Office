package fr.insee.rmes.bauhaus_services.concepts.concepts;

import static fr.insee.rmes.bauhaus_services.concepts.OdtExportStubs.assertSameResponseExportedOnceAsOdt;
import static fr.insee.rmes.bauhaus_services.concepts.OdtExportStubs.givenOdtExportResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import fr.insee.rmes.Constants;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.concepts.ConceptForExport;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationService;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.ExportUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ConceptsExportBuilderTest {

    @Mock
    private LegacyConceptsRepository legacyConceptsRepository;

    @Mock
    private ExportUtils exportUtils;

    @Mock
    private RepositoryGestion repoGestion;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private ConceptConceptsQueries conceptConceptsQueries;

    private ConceptsExportBuilder conceptsExportBuilder;

    @BeforeEach
    void setUp() {
        conceptsExportBuilder = new ConceptsExportBuilder(
                repoGestion,
                null,
                null,
                null,
                legacyConceptsRepository,
                organisationService,
                exportUtils,
                conceptConceptsQueries);
    }

    @Test
    void shouldGetConceptData() throws RmesException {
        // Given
        String id = "c1";
        JSONObject conceptJson = conceptJsonWith("Creator", "Contributor", "Validated")
                .put("prefLabelLg2", "Concept EN")
                .put("modified", "2025-01-02T00:00:00")
                .put("valid", "2025-12-31T00:00:00");

        JSONArray links = new JSONArray()
                .put(new JSONObject()
                        .put("typeOfLink", "broader")
                        .put(Constants.PREF_LABEL_LG1, "Broader Concept FR")
                        .put(Constants.PREF_LABEL_LG2, "Broader Concept EN"));

        JSONObject notes =
                new JSONObject().put("definitionLg1", "Definition FR").put("definitionLg2", "Definition EN");

        when(legacyConceptsRepository.getConceptById(id)).thenReturn(conceptJson);
        when(repoGestion.getResponseAsArray(any())).thenReturn(links);
        when(repoGestion.getResponseAsObject(any())).thenReturn(notes);

        // When
        ConceptForExport result = conceptsExportBuilder.getConceptData(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Concept FR", result.getPrefLabelLg1());
        assertEquals("Concept EN", result.getPrefLabelLg2());
        verify(legacyConceptsRepository, times(1)).getConceptById(id);
        verify(repoGestion, times(1)).getResponseAsArray(any());
        verify(repoGestion, times(1)).getResponseAsObject(any());
    }

    @Test
    void shouldGetConceptDataWithAltLabels() throws RmesException {
        // Given
        String id = "c1";
        JSONObject conceptJson = conceptJsonWith("Creator", "Contributor", "Unpublished")
                .put(Constants.ALT_LABEL_LG1, new JSONArray().put("Alt 1").put("Alt 2"))
                .put(Constants.ALT_LABEL_LG2, new JSONArray().put("Alt EN 1"));

        givenConceptWithoutLinksNorNotes(id, conceptJson);

        // When
        ConceptForExport result = conceptsExportBuilder.getConceptData(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        // Alt labels should be transformed to string
        verify(legacyConceptsRepository, times(1)).getConceptById(id);
    }

    @Test
    void shouldExportAsResponse() throws RmesException {
        // Given
        String fileName = "concept-export";
        Map<String, String> xmlContent = conceptXmlContent();
        ResponseEntity<?> expectedResponse = givenOdtExportResponse(exportUtils);

        // When
        ResponseEntity<?> result = conceptsExportBuilder.exportAsResponse(fileName, xmlContent, true, true, false);

        // Then
        assertSameResponseExportedOnceAsOdt(exportUtils, expectedResponse, result);
    }

    @Test
    void shouldExportAsResponseWithDifferentLanguages() throws RmesException {
        // Given
        String fileName = "concept-export";
        Map<String, String> xmlContent = conceptXmlContent();
        ResponseEntity<?> expectedResponse = givenOdtExportResponse(exportUtils);

        // When - Export with lg1 only
        ResponseEntity<?> result1 = conceptsExportBuilder.exportAsResponse(fileName, xmlContent, true, false, false);

        // Then
        assertNotNull(result1);
        assertEquals(expectedResponse, result1);

        // When - Export with lg2 only
        ResponseEntity<?> result2 = conceptsExportBuilder.exportAsResponse(fileName, xmlContent, false, true, false);

        // Then
        assertNotNull(result2);
        assertEquals(expectedResponse, result2);

        verify(exportUtils, times(2))
                .exportAsODT(anyString(), anyMap(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldExportAsResponseWithEmptyFields() throws RmesException {
        // Given
        String fileName = "concept-export";
        Map<String, String> xmlContent = conceptXmlContent();
        ResponseEntity<?> expectedResponse = givenOdtExportResponse(exportUtils);

        // When - Export with empty fields included
        ResponseEntity<?> result = conceptsExportBuilder.exportAsResponse(fileName, xmlContent, true, true, true);

        // Then
        assertSameResponseExportedOnceAsOdt(exportUtils, expectedResponse, result);
    }

    @Test
    void shouldExportAsInputStream() throws RmesException {
        // Given
        String fileName = "concept-export";
        Map<String, String> xmlContent = conceptXmlContent();
        InputStream expectedStream = new ByteArrayInputStream(new byte[0]);

        when(exportUtils.exportAsInputStream(
                        anyString(), anyMap(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(expectedStream);

        // When
        InputStream result = conceptsExportBuilder.exportAsInputStream(fileName, xmlContent, true, true, false);

        // Then
        assertNotNull(result);
        assertEquals(expectedStream, result);
        verify(exportUtils, times(1))
                .exportAsInputStream(
                        anyString(), anyMap(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldTransformAltLabelListToString() throws RmesException {
        // Given
        String id = "c1";
        JSONArray altLabels = new JSONArray().put("Label 1").put("Label 2").put("Label 3");

        JSONObject conceptJson =
                conceptJsonWith("Creator", "Contributor", "Validated").put(Constants.ALT_LABEL_LG1, altLabels);

        givenConceptWithoutLinksNorNotes(id, conceptJson);

        // When
        ConceptForExport result = conceptsExportBuilder.getConceptData(id);

        // Then
        assertNotNull(result);
        // The alt labels should have been transformed to a string
    }

    @Test
    void shouldHandleConceptWithNoNotes() throws RmesException {
        // Given
        String id = "c1";
        JSONObject conceptJson = conceptJsonWith("Creator", "Contributor", "Validated");

        givenConceptWithoutLinksNorNotes(id, conceptJson);

        // When
        ConceptForExport result = conceptsExportBuilder.getConceptData(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void shouldHandleConceptWithNoLinks() throws RmesException {
        // Given
        String id = "c1";
        JSONObject conceptJson = conceptJsonWith("Creator", "Contributor", "Unpublished");

        givenConceptWithoutLinksNorNotes(id, conceptJson);

        // When
        ConceptForExport result = conceptsExportBuilder.getConceptData(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void shouldFormatDatesCorrectly() throws RmesException {
        // Given
        String id = "c1";
        JSONObject conceptJson = conceptJsonWith("Creator", "Contributor", "Validated")
                .put("created", "2025-01-15T10:30:00")
                .put("modified", "2025-02-20T14:45:00")
                .put("valid", "2025-12-31T23:59:59");

        givenConceptWithoutLinksNorNotes(id, conceptJson);

        // When
        ConceptForExport result = conceptsExportBuilder.getConceptData(id);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCreated());
        // Dates should be formatted
    }

    private static Map<String, String> conceptXmlContent() {
        Map<String, String> xmlContent = new HashMap<>();
        xmlContent.put("conceptFile", "<Concept></Concept>");
        return xmlContent;
    }

    private void givenConceptWithoutLinksNorNotes(String id, JSONObject conceptJson) throws RmesException {
        when(legacyConceptsRepository.getConceptById(id)).thenReturn(conceptJson);
        when(repoGestion.getResponseAsArray(any())).thenReturn(new JSONArray());
        when(repoGestion.getResponseAsObject(any())).thenReturn(new JSONObject());
    }

    private JSONObject conceptJsonWith(String creator, String contributor) {
        return conceptJsonWith(creator, contributor, "Validated");
    }

    private JSONObject conceptJsonWith(String creator, String contributor, String isValidated) {
        return new JSONObject()
                .put("id", "c1")
                .put("prefLabelLg1", "Concept FR")
                .put("created", "2025-01-01T00:00:00")
                .put("isValidated", isValidated)
                .put("disseminationStatus", "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique")
                .put("creator", creator)
                .put("contributor", contributor)
                .put("conceptVersion", "1");
    }

    @Test
    void shouldResolveCreatorAndContributorIntoOrganisationLabelsWithASingleBatchLookup() throws RmesException {
        // Given un creator stocké en littéral HIE et un contributor stocké en IRI d'organisation
        String creator = "HIE2000069";
        String contributor = "http://bauhaus/organisations/insee/HIE2003216";

        givenConceptWithoutLinksNorNotes("c1", conceptJsonWith(creator, contributor));
        when(organisationService.getOrganisationsMap(List.of(creator, contributor)))
                .thenReturn(Map.of(
                        creator, new OrganisationOption(creator, "Division Enquêtes thématiques et études régionales"),
                        contributor, new OrganisationOption("DG75-C901", "Département des Ressources humaines (DRH)")));

        // When
        ConceptForExport result = conceptsExportBuilder.getConceptData("c1");

        // Then
        assertEquals("Division Enquêtes thématiques et études régionales", result.getCreator());
        assertEquals("Département des Ressources humaines (DRH)", result.getContributor());
        verify(organisationService, times(1)).getOrganisationsMap(anyList());
    }

    @Test
    void shouldFallBackToAReadableIdentifierWhenOrganisationIsUnknown() throws RmesException {
        // Given des organisations introuvables dans le référentiel
        String creator = "http://bauhaus/organisations/insee/HIE000000";
        String contributor = "DG75-L201";

        givenConceptWithoutLinksNorNotes("c1", conceptJsonWith(creator, contributor));
        when(organisationService.getOrganisationsMap(anyList())).thenReturn(Map.of());

        // When
        ConceptForExport result = conceptsExportBuilder.getConceptData("c1");

        // Then : l'IRI est raccourci à son dernier segment, le littéral est conservé tel quel
        assertEquals("HIE000000", result.getCreator());
        assertEquals("DG75-L201", result.getContributor());
    }

    @Test
    void shouldKeepRawStampsWhenOrganisationLookupFails() throws RmesException {
        // Given une résolution des organisations qui échoue
        givenConceptWithoutLinksNorNotes("c1", conceptJsonWith("HIE2000069", "DG75-L201"));
        when(organisationService.getOrganisationsMap(anyList()))
                .thenThrow(new RmesException(500, "SPARQL failure", "organisations"));

        // When : l'export ne doit pas échouer pour autant
        ConceptForExport result = conceptsExportBuilder.getConceptData("c1");

        // Then
        assertEquals("HIE2000069", result.getCreator());
        assertEquals("DG75-L201", result.getContributor());
    }
}
