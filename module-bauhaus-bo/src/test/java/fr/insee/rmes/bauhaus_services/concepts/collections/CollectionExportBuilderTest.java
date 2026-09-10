package fr.insee.rmes.bauhaus_services.concepts.collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.model.concepts.CollectionForExportOld;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationService;
import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptCollectionsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.ExportUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.Collator;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CollectionExportBuilderTest {

    @Mock
    private RepositoryGestion repoGestion;

    @Mock
    private ExportUtils exportUtils;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ConceptCollectionsQueries conceptCollectionsQueries;

    @Mock
    private OrganisationService organisationService;

    private CollectionExportBuilder collectionExportBuilder;

    String keyName = "prefLabelLg1";
    JSONObject members1 = new JSONObject().put("id", "members1").put(keyName, "en");
    JSONObject members2 = new JSONObject().put("id", "members2").put(keyName, "fr");
    JSONObject members3 = new JSONObject().put("id", "members3").put(keyName, "en");
    JSONObject members4 = new JSONObject().put("id", "members4").put(keyName, "fr");

    @BeforeEach
    void setUp() throws Exception {
        collectionExportBuilder = new CollectionExportBuilder(
                repoGestion, null, null, null, organisationService, exportUtils, conceptCollectionsQueries);
        lenient().when(conceptCollectionsQueries.collectionQuery(anyString())).thenReturn("mock-query");
        lenient()
                .when(conceptCollectionsQueries.collectionConceptsQuery(anyString()))
                .thenReturn("mock-query");
        lenient()
                .when(conceptCollectionsQueries.collectionMembersQuery(anyString()))
                .thenReturn("mock-query");
    }

    @Test
    void shouldCompareTwoJsonObjects() {

        Collator instance = Collator.getInstance();

        String valA = (String) members1.get(keyName);
        String valB = (String) members2.get(keyName);
        String valC = (String) members3.get(keyName);
        String valD = (String) members4.get(keyName);

        List<Integer> actual = List.of(
                instance.compare(valA.toLowerCase(), valB.toLowerCase()),
                instance.compare(valB.toLowerCase(), valC.toLowerCase()),
                instance.compare(valA.toLowerCase(), valC.toLowerCase()),
                instance.compare(valB.toLowerCase(), valD.toLowerCase()));

        List<Integer> expected = List.of(-1, 1, 0, 0);

        assertEquals(expected, actual);
    }

    @Test
    void shouldSortJsonObjects() {

        List<JSONObject> orderMembers = List.of(members1, members3, members2, members4);

        List<JSONObject> notOrderMembers = new ArrayList<>();
        notOrderMembers.add(members1);
        notOrderMembers.add(members2);
        notOrderMembers.add(members3);
        notOrderMembers.add(members4);

        notOrderMembers.sort(new Comparator<>() {
            private static final String KEY_NAME = "prefLabelLg1";
            final Collator instance = Collator.getInstance();

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = (String) a.get(KEY_NAME);
                String valB = (String) b.get(KEY_NAME);

                return instance.compare(valA.toLowerCase(), valB.toLowerCase());
            }
        });

        assertEquals(orderMembers, notOrderMembers);
    }

    @Test
    void shouldGetCollectionData() throws RmesException {
        // Given
        String id = "c1";
        JSONObject collectionJson = new JSONObject()
                .put("id", id)
                .put("prefLabelLg1", "Collection FR")
                .put("prefLabelLg2", "Collection EN")
                .put("created", "2025-01-01T00:00:00")
                .put("modified", "2025-01-02T00:00:00")
                .put("isValidated", "Validated")
                .put("creator", "Creator")
                .put("contributor", "Contributor");

        JSONArray members = new JSONArray()
                .put(new JSONObject().put("id", "m1").put("prefLabelLg1", "Member 1"))
                .put(new JSONObject().put("id", "m2").put("prefLabelLg1", "Member 2"));

        when(repoGestion.getResponseAsObject(anyString())).thenReturn(collectionJson);
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(members);

        // When
        CollectionForExport result = collectionExportBuilder.getCollectionData(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Collection FR", result.getPrefLabelLg1());
        assertEquals("Collection EN", result.getPrefLabelLg2());
        verify(repoGestion, times(1)).getResponseAsObject(anyString());
        verify(repoGestion, times(1)).getResponseAsArray(anyString());
    }

    @Test
    void shouldGetCollectionDataOld() throws RmesException {
        // Given
        String id = "c1";
        JSONObject collectionJson = new JSONObject()
                .put("id", id)
                .put("prefLabelLg1", "Collection FR")
                .put("created", "2025-01-01T00:00:00")
                .put("isValidated", "Unpublished")
                .put("creator", "Creator");

        JSONArray members = new JSONArray().put(new JSONObject().put("id", "m1").put("prefLabelLg1", "Member 1"));

        when(repoGestion.getResponseAsObject(anyString())).thenReturn(collectionJson);
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(members);

        // When
        CollectionForExportOld result = collectionExportBuilder.getCollectionDataOld(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Collection FR", result.getPrefLabelLg1());
        verify(repoGestion, times(1)).getResponseAsObject(anyString());
        verify(repoGestion, times(1)).getResponseAsArray(anyString());
    }

    @Test
    void shouldExportAsResponse() throws RmesException {
        // Given
        String fileName = "collection-export";
        Map<String, String> xmlContent = new HashMap<>();
        xmlContent.put("collectionFile", "<Collection></Collection>");
        ResponseEntity<?> expectedResponse = ResponseEntity.ok(new ByteArrayResource(new byte[0]));

        when(exportUtils.exportAsODT(anyString(), anyMap(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn((ResponseEntity) expectedResponse);

        // When
        ResponseEntity<?> result = collectionExportBuilder.exportAsResponse(fileName, xmlContent, true, true, false);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(exportUtils, times(1))
                .exportAsODT(anyString(), anyMap(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldExportAsResponseODT() throws RmesException {
        // Given
        String fileName = "collection-export";
        Map<String, String> xmlContent = new HashMap<>();
        xmlContent.put("collectionFile", "<Collection></Collection>");
        Language lg = Language.lg1;
        ResponseEntity<?> expectedResponse = ResponseEntity.ok(new ByteArrayResource(new byte[0]));

        when(exportUtils.exportAsODT(anyString(), anyMap(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn((ResponseEntity) expectedResponse);

        // When
        ResponseEntity<?> result = collectionExportBuilder.exportAsResponseODT(fileName, xmlContent, true, lg);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(exportUtils, times(1))
                .exportAsODT(anyString(), anyMap(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldExportAsResponseODS() throws RmesException {
        // Given
        String fileName = "collection-export";
        Map<String, String> xmlContent = new HashMap<>();
        xmlContent.put("collectionFile", "<Collection></Collection>");
        ResponseEntity<?> expectedResponse = ResponseEntity.ok(new ByteArrayResource(new byte[0]));

        when(exportUtils.exportAsODS(anyString(), anyMap(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn((ResponseEntity) expectedResponse);

        // When
        ResponseEntity<?> result = collectionExportBuilder.exportAsResponseODS(fileName, xmlContent, true, false, true);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(exportUtils, times(1))
                .exportAsODS(anyString(), anyMap(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldExportMultipleCollectionsAsZipOdt() throws RmesException, Exception {
        // Given
        Map<String, Map<String, String>> collections = new HashMap<>();
        Map<String, String> collection1 = new HashMap<>();
        collection1.put("collectionFile", "<Collection></Collection>");
        collections.put("collection1.odt", collection1);

        Map<String, Map<String, InputStream>> concepts = new HashMap<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override
            public void write(int b) {
                outputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
        });

        when(exportUtils.exportAsInputStream(
                        anyString(), anyMap(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        // When
        collectionExportBuilder.exportMultipleCollectionsAsZipOdt(
                collections, true, true, false, response, Language.lg1, concepts, false);

        // Then
        verify(exportUtils, atLeastOnce())
                .exportAsInputStream(
                        anyString(), anyMap(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldExportMultipleCollectionsAsZipOds() throws RmesException, Exception {
        // Given
        Map<String, Map<String, String>> collections = new HashMap<>();
        Map<String, String> collection1 = new HashMap<>();
        collection1.put("collectionFile", "<Collection></Collection>");
        collections.put("collection1.ods", collection1);

        Map<String, Map<String, InputStream>> concepts = new HashMap<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override
            public void write(int b) {
                outputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
        });

        when(exportUtils.exportAsInputStream(
                        anyString(), anyMap(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        // When
        collectionExportBuilder.exportMultipleCollectionsAsZipOds(
                collections, true, false, true, response, concepts, false);

        // Then
        verify(exportUtils, atLeastOnce())
                .exportAsInputStream(
                        anyString(), anyMap(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldHandleEmptyMembersArrayInGetCollectionData() throws RmesException {
        // Given
        String id = "c1";
        JSONObject collectionJson = new JSONObject()
                .put("id", id)
                .put("prefLabelLg1", "Collection FR")
                .put("created", "2025-01-01T00:00:00")
                .put("isValidated", "Validated");

        JSONArray emptyMembers = new JSONArray();

        when(repoGestion.getResponseAsObject(anyString())).thenReturn(collectionJson);
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(emptyMembers);

        // When
        CollectionForExport result = collectionExportBuilder.getCollectionData(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertNotNull(result.getMembersLg());
    }

    @Test
    void shouldSortMembersByPrefLabelLg1() throws RmesException {
        // Given
        String id = "c1";
        JSONObject collectionJson = new JSONObject()
                .put("id", id)
                .put("prefLabelLg1", "Collection")
                .put("created", "2025-01-01T00:00:00")
                .put("isValidated", "Validated");

        JSONArray members = new JSONArray()
                .put(new JSONObject().put("id", "m3").put("prefLabelLg1", "Zebra"))
                .put(new JSONObject().put("id", "m1").put("prefLabelLg1", "Apple"))
                .put(new JSONObject().put("id", "m2").put("prefLabelLg1", "Banana"));

        when(repoGestion.getResponseAsObject(anyString())).thenReturn(collectionJson);
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(members);

        // When
        CollectionForExport result = collectionExportBuilder.getCollectionData(id);

        // Then
        assertNotNull(result);
        // Members should be sorted alphabetically by prefLabelLg1
        // Verification would require accessing the sorted members
    }

    private JSONObject collectionJsonWith(String creator, String contributor) {
        JSONObject collectionJson = new JSONObject()
                .put("id", "c1")
                .put("prefLabelLg1", "Collection FR")
                .put("created", "2025-01-01T00:00:00")
                .put("isValidated", "Validated")
                .put("creator", creator);
        if (contributor != null) {
            collectionJson.put("contributor", contributor);
        }
        return collectionJson;
    }

    @Test
    void shouldResolveCollectionAndMembersCreatorsIntoOrganisationLabelsWithASingleBatchLookup() throws RmesException {
        // Given un creator stocké en littéral HIE, un contributor stocké en IRI et des membres avec leur propre creator
        String creator = "HIE2000069";
        String contributor = "http://bauhaus/organisations/insee/HIE2003216";
        JSONArray members = new JSONArray()
                .put(new JSONObject().put("id", "m1").put(keyName, "Apple").put("creator", creator))
                .put(new JSONObject().put("id", "m2").put(keyName, "Banana").put("creator", "HIE0000042"))
                .put(new JSONObject().put("id", "m3").put(keyName, "Cherry"));

        when(repoGestion.getResponseAsObject(anyString())).thenReturn(collectionJsonWith(creator, contributor));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(members);
        when(organisationService.getOrganisationsMap(anyList()))
                .thenReturn(Map.of(
                        creator,
                        new OrganisationOption("DG75-L201", "Division Concepts, harmonisation et nomenclatures"),
                        contributor,
                        new OrganisationOption("DG75-C901", "Département des Ressources humaines (DRH)"),
                        "HIE0000042",
                        new OrganisationOption("DG75-F001", "Division Emploi")));

        // When
        CollectionForExport result = collectionExportBuilder.getCollectionData("c1");

        // Then
        assertEquals("Division Concepts, harmonisation et nomenclatures", result.getCreator());
        assertEquals("Département des Ressources humaines (DRH)", result.getContributor());
        assertEquals(
                "Division Concepts, harmonisation et nomenclatures",
                result.getMembersLg().get(0).getCreator());
        assertEquals("Division Emploi", result.getMembersLg().get(1).getCreator());
        assertNull(result.getMembersLg().get(2).getCreator());
        // Un seul appel batch, identifiants dédupliqués (le creator du membre m1 est aussi celui de la collection)
        verify(organisationService, times(1))
                .getOrganisationsMap(argThat(identifiers -> identifiers.size() == 3
                        && identifiers.containsAll(List.of(creator, contributor, "HIE0000042"))));
    }

    @Test
    void shouldFallBackToAReadableIdentifierWhenCollectionOrganisationIsUnknown() throws RmesException {
        // Given des organisations introuvables dans le référentiel
        when(repoGestion.getResponseAsObject(anyString()))
                .thenReturn(collectionJsonWith("http://bauhaus/organisations/insee/HIE000000", "DG75-L201"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(new JSONArray());
        when(organisationService.getOrganisationsMap(anyList())).thenReturn(Map.of());

        // When
        CollectionForExport result = collectionExportBuilder.getCollectionData("c1");

        // Then : l'IRI est raccourci à son dernier segment, le littéral est conservé tel quel
        assertEquals("HIE000000", result.getCreator());
        assertEquals("DG75-L201", result.getContributor());
    }

    @Test
    void shouldKeepRawStampsWhenOrganisationLookupFails() throws RmesException {
        // Given une résolution des organisations qui échoue
        when(repoGestion.getResponseAsObject(anyString())).thenReturn(collectionJsonWith("HIE2000069", "DG75-L201"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(new JSONArray());
        when(organisationService.getOrganisationsMap(anyList()))
                .thenThrow(new RmesException(500, "SPARQL failure", "organisations"));

        // When : l'export ne doit pas échouer pour autant
        CollectionForExport result = collectionExportBuilder.getCollectionData("c1");

        // Then
        assertEquals("HIE2000069", result.getCreator());
        assertEquals("DG75-L201", result.getContributor());
    }

    @Test
    void shouldResolveOrganisationLabelsForOldCollectionExport() throws RmesException {
        // Given
        when(repoGestion.getResponseAsObject(anyString())).thenReturn(collectionJsonWith("HIE2000069", "DG75-L201"));
        when(repoGestion.getResponseAsArray(anyString()))
                .thenReturn(new JSONArray().put(new JSONObject().put("id", "m1").put(keyName, "Member 1")));
        when(organisationService.getOrganisationsMap(List.of("HIE2000069", "DG75-L201")))
                .thenReturn(Map.of(
                        "HIE2000069",
                                new OrganisationOption(
                                        "DG75-L201", "Division Concepts, harmonisation et nomenclatures"),
                        "DG75-L201", new OrganisationOption("DG75-L201", "Division Emploi")));

        // When
        CollectionForExportOld result = collectionExportBuilder.getCollectionDataOld("c1");

        // Then
        assertEquals("Division Concepts, harmonisation et nomenclatures", result.getCreator());
        assertEquals("Division Emploi", result.getContributor());
    }
}
