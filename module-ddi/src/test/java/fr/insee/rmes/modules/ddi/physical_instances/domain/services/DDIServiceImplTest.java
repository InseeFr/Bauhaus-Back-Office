package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidSentinelValuesException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CategoryCodeListUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnitResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UsageItem;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DDIServiceImplTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-08-03T08:00:00Z"), ZoneOffset.ofHours(2));

    @Mock
    private DDIRepository ddiRepository;

    @Mock
    private SeriesCreatorsPort seriesCreatorsPort;

    private DDIServiceImpl ddiService;

    @BeforeEach
    void setUp() {
        ddiService = new DDIServiceImpl(ddiRepository, seriesCreatorsPort, FIXED_CLOCK);
    }

    @Test
    void shouldGetPhysicalInstances() {
        // Given
        List<PartialPhysicalInstance> expectedInstances = List.of(
                new PartialPhysicalInstance("pi-1", "Physical Instance 1", new Date(), "fr.insee"),
                new PartialPhysicalInstance("pi-2", "Physical Instance 2", new Date(), "fr.insee"),
                new PartialPhysicalInstance("pi-3", "Physical Instance 3", new Date(), "fr.insee"));
        when(ddiRepository.getPhysicalInstancesViaAdvancedQuery()).thenReturn(expectedInstances);

        // When
        List<PartialPhysicalInstance> result = ddiService.getPhysicalInstances();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("pi-1", result.get(0).id());
        assertEquals("Physical Instance 1", result.get(0).label());
        assertEquals("pi-2", result.get(1).id());
        assertEquals("Physical Instance 2", result.get(1).label());
        assertEquals("pi-3", result.get(2).id());
        assertEquals("Physical Instance 3", result.get(2).label());

        verify(ddiRepository).getPhysicalInstancesViaAdvancedQuery();
    }

    @Test
    void getPhysicalInstances_shouldBeSortedByLabelAscending() {
        when(ddiRepository.getPhysicalInstancesViaAdvancedQuery())
                .thenReturn(List.of(
                        new PartialPhysicalInstance("pi-c", "Charlie", new Date(), "fr.insee"),
                        new PartialPhysicalInstance("pi-a", "alpha", new Date(), "fr.insee"),
                        new PartialPhysicalInstance("pi-b", "Bravo", new Date(), "fr.insee")));

        List<PartialPhysicalInstance> result = ddiService.getPhysicalInstances();

        assertEquals(
                List.of("alpha", "Bravo", "Charlie"),
                result.stream().map(PartialPhysicalInstance::label).toList());
    }

    @Test
    void getPhysicalInstancesFilteredByStamp_shouldBeSortedByLabelAscending() {
        PartialPhysicalInstance piC = new PartialPhysicalInstance("pi-c", "Charlie", new Date(), "fr.insee");
        PartialPhysicalInstance piA = new PartialPhysicalInstance("pi-a", "alpha", new Date(), "fr.insee");
        when(ddiRepository.getPhysicalInstancesViaAdvancedQuery()).thenReturn(List.of(piC, piA));
        when(ddiRepository.getPhysicalInstanceParents("fr.insee", "pi-c"))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su", "fr.insee", "g1"));
        when(ddiRepository.getPhysicalInstanceParents("fr.insee", "pi-a"))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su", "fr.insee", "g1"));
        String iri = "http://id.insee.fr/operations/serie/s1";
        when(ddiRepository.getGroup("fr.insee", "g1")).thenReturn(groupResponseWithSeries("g1", iri));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri))).thenReturn(Map.of(iri, List.of("stamp-A")));

        List<PartialPhysicalInstance> result = ddiService.getPhysicalInstancesFilteredByStamp(Set.of("stamp-A"));

        assertEquals(
                List.of("alpha", "Charlie"),
                result.stream().map(PartialPhysicalInstance::label).toList());
    }

    @Test
    void searchPhysicalInstances_returnsRepositoryRowsSortedByLabelWithResolvedParentLabels() {
        PhysicalInstanceSearchRow charlie = new PhysicalInstanceSearchRow(
                "fr.insee",
                "pi-c",
                "Charlie",
                new Date(),
                "fr.insee",
                "su-1",
                "Study One",
                "fr.insee",
                "g1",
                "Group One");
        PhysicalInstanceSearchRow alpha = new PhysicalInstanceSearchRow(
                "fr.insee",
                "pi-a",
                "alpha",
                new Date(),
                "fr.insee",
                "su-1",
                "Study One",
                "fr.insee",
                "g1",
                "Group One");
        when(ddiRepository.getPhysicalInstanceSearchRows()).thenReturn(List.of(charlie, alpha));

        List<PhysicalInstanceSearchRow> rows = ddiService.searchPhysicalInstances();

        assertEquals(
                List.of("alpha", "Charlie"),
                rows.stream().map(PhysicalInstanceSearchRow::label).toList());
        assertEquals("Study One", rows.get(0).studyUnitLabel());
        assertEquals("Group One", rows.get(0).groupLabel());
    }

    @Test
    void searchPhysicalInstancesFilteredByStamp_keepsRowsWhoseGroupCreatorMatchesAndDropsOrphans() {
        String iri = "http://id.insee.fr/operations/serie/s1";
        PhysicalInstanceSearchRow kept = new PhysicalInstanceSearchRow(
                "fr.insee", "pi-1", "Kept", new Date(), "fr.insee", "su-1", "Study One", "fr.insee", "g1", "Group One");
        PhysicalInstanceSearchRow orphan = new PhysicalInstanceSearchRow(
                "fr.insee", "pi-3", "Orphan", new Date(), null, null, null, null, null, null);
        when(ddiRepository.getPhysicalInstanceSearchRows()).thenReturn(List.of(kept, orphan));
        when(ddiRepository.getGroup("fr.insee", "g1")).thenReturn(groupResponseWithSeries("g1", iri));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri))).thenReturn(Map.of(iri, List.of("stamp-A")));

        List<PhysicalInstanceSearchRow> rows = ddiService.searchPhysicalInstancesFilteredByStamp(Set.of("stamp-A"));

        assertEquals(
                List.of("Kept"),
                rows.stream().map(PhysicalInstanceSearchRow::label).toList());
    }

    @Test
    void shouldGetLogicalProducts() {
        // Given
        List<PartialLogicalProduct> expectedProducts = List.of(
                new PartialLogicalProduct("lp-1", "Logical Product 1", new Date(), "fr.insee"),
                new PartialLogicalProduct("lp-2", "Logical Product 2", new Date(), "fr.insee"));
        when(ddiRepository.getLogicalProducts()).thenReturn(expectedProducts);

        // When
        List<PartialLogicalProduct> result = ddiService.getLogicalProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("lp-1", result.get(0).id());
        assertEquals("Logical Product 1", result.get(0).label());
        assertEquals("lp-2", result.get(1).id());
        assertEquals("Logical Product 2", result.get(1).label());

        verify(ddiRepository).getLogicalProducts();
    }

    @Test
    void shouldGetLogicalProductsByGroup() {
        // Given
        List<PartialLogicalProduct> expectedProducts =
                List.of(new PartialLogicalProduct("lp-1", "Produit Logique 1", new Date(), "fr.insee"));
        when(ddiRepository.getLogicalProductsByGroup("fr.insee", "group-1")).thenReturn(expectedProducts);

        // When
        List<PartialLogicalProduct> result = ddiService.getLogicalProductsByGroup("fr.insee", "group-1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("lp-1", result.get(0).id());

        verify(ddiRepository).getLogicalProductsByGroup("fr.insee", "group-1");
    }

    @Test
    void shouldGetCodeListSchemes() {
        // Given
        List<PartialCodeListScheme> expected = List.of(
                new PartialCodeListScheme("cls-1", "Schéma 1", new Date(), "fr.insee"),
                new PartialCodeListScheme("cls-2", "Schéma 2", new Date(), "fr.insee"));
        when(ddiRepository.getCodeListSchemes()).thenReturn(expected);

        // When
        List<PartialCodeListScheme> result = ddiService.getCodeListSchemes();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("cls-1", result.get(0).id());

        verify(ddiRepository).getCodeListSchemes();
    }

    @Test
    void shouldGetCodeListSchemesByLogicalProduct() {
        // Given
        List<PartialCodeListScheme> expectedSchemes =
                List.of(new PartialCodeListScheme("cls-1", "Schéma 1", new Date(), "fr.insee"));
        when(ddiRepository.getCodeListSchemesByLogicalProduct("fr.insee", "lp-1"))
                .thenReturn(expectedSchemes);

        // When
        List<PartialCodeListScheme> result = ddiService.getCodeListSchemesByLogicalProduct("fr.insee", "lp-1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("cls-1", result.get(0).id());

        verify(ddiRepository).getCodeListSchemesByLogicalProduct("fr.insee", "lp-1");
    }

    @Test
    void shouldGetCodeListsByCodeListScheme() {
        // Given
        List<PartialCodesList> expected =
                List.of(new PartialCodesList("code-list-1", "Liste 1", new Date(), "fr.insee"));
        when(ddiRepository.getCodeListsByCodeListScheme("fr.insee", "cls-1")).thenReturn(expected);

        // When
        List<PartialCodesList> result = ddiService.getCodeListsByCodeListScheme("fr.insee", "cls-1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("code-list-1", result.get(0).id());

        verify(ddiRepository).getCodeListsByCodeListScheme("fr.insee", "cls-1");
    }

    @Test
    void shouldAggregateCodeListsAcrossAllLogicalProductsAndSchemesOfGroup() {
        // Given : group -> 2 LP, chaque LP -> 1 CLS, chaque CLS -> des listes de codes,
        // avec une liste partagée (cl-shared) entre les deux schémas.
        when(ddiRepository.getLogicalProductsByGroup("fr.insee", "group-1"))
                .thenReturn(List.of(
                        new PartialLogicalProduct("lp-1", "Produit Logique 1", new Date(), "fr.insee"),
                        new PartialLogicalProduct("lp-2", "Produit Logique 2", new Date(), "fr.insee")));
        when(ddiRepository.getCodeListSchemesByLogicalProduct("fr.insee", "lp-1"))
                .thenReturn(List.of(new PartialCodeListScheme("cls-1", "Schéma 1", new Date(), "fr.insee")));
        when(ddiRepository.getCodeListSchemesByLogicalProduct("fr.insee", "lp-2"))
                .thenReturn(List.of(new PartialCodeListScheme("cls-2", "Schéma 2", new Date(), "fr.insee")));
        when(ddiRepository.getCodeListsByCodeListScheme("fr.insee", "cls-1"))
                .thenReturn(List.of(
                        new PartialCodesList("cl-1", "Liste 1", new Date(), "fr.insee"),
                        new PartialCodesList("cl-shared", "Liste partagée", new Date(), "fr.insee")));
        when(ddiRepository.getCodeListsByCodeListScheme("fr.insee", "cls-2"))
                .thenReturn(List.of(
                        new PartialCodesList("cl-2", "Liste 2", new Date(), "fr.insee"),
                        new PartialCodesList("cl-shared", "Liste partagée", new Date(), "fr.insee")));

        // When
        List<PartialCodesList> result = ddiService.getCodeListsByGroup("fr.insee", "group-1");

        // Then : toutes les listes de tous les CLS de tous les LP, dédupliquées par agency/id.
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(
                Set.of("cl-1", "cl-2", "cl-shared"),
                result.stream().map(PartialCodesList::id).collect(java.util.stream.Collectors.toSet()));

        verify(ddiRepository).getLogicalProductsByGroup("fr.insee", "group-1");
        verify(ddiRepository).getCodeListSchemesByLogicalProduct("fr.insee", "lp-1");
        verify(ddiRepository).getCodeListSchemesByLogicalProduct("fr.insee", "lp-2");
        verify(ddiRepository).getCodeListsByCodeListScheme("fr.insee", "cls-1");
        verify(ddiRepository).getCodeListsByCodeListScheme("fr.insee", "cls-2");
    }

    /** Valeurs sentinelles (#1566) : le service délègue la liste des MMVR réutilisables au repository. */
    @Test
    void shouldGetMissingValuesRepresentationsByGroup() {
        List<PartialMissingValuesRepresentation> expected = List.of(new PartialMissingValuesRepresentation(
                "mmvr-1", "fr.insee", "1", "Valeurs sentinelles NSP/REF", "cl-sentinelles", List.of("NSP", "REF")));
        when(ddiRepository.getMissingValuesRepresentationsByGroup("fr.insee", "group-1"))
                .thenReturn(expected);

        List<PartialMissingValuesRepresentation> result =
                ddiService.getMissingValuesRepresentationsByGroup("fr.insee", "group-1");

        assertEquals(expected, result);
    }

    @Test
    void shouldExcludeSentinelCodeListsFromGroupCodeLists() {
        // Given : le CLS du groupe contient une liste « classique » et la liste de valeurs
        // sentinelles (cf. #1566), cette dernière étant aussi référencée par une MMVR du groupe.
        when(ddiRepository.getLogicalProductsByGroup("fr.insee", "group-1"))
                .thenReturn(List.of(new PartialLogicalProduct("lp-1", "Produit Logique 1", new Date(), "fr.insee")));
        when(ddiRepository.getCodeListSchemesByLogicalProduct("fr.insee", "lp-1"))
                .thenReturn(List.of(new PartialCodeListScheme("cls-1", "Schéma 1", new Date(), "fr.insee")));
        when(ddiRepository.getCodeListsByCodeListScheme("fr.insee", "cls-1"))
                .thenReturn(List.of(
                        new PartialCodesList("cl-1", "Liste 1", new Date(), "fr.insee"),
                        new PartialCodesList("cl-sentinel", "Valeurs sentinelles", new Date(), "fr.insee")));
        when(ddiRepository.getMissingCodesListsByGroup("fr.insee", "group-1"))
                .thenReturn(
                        List.of(new PartialCodesList("cl-sentinel", "Valeurs sentinelles", new Date(), "fr.insee")));

        // When
        List<PartialCodesList> result = ddiService.getCodeListsByGroup("fr.insee", "group-1");

        // Then : la liste sentinelle est exclue des listes de codes du groupe.
        assertEquals(1, result.size());
        assertEquals("cl-1", result.get(0).id());
    }

    @Test
    void shouldReturnEmptyListWhenGroupHasNoLogicalProduct() {
        when(ddiRepository.getLogicalProductsByGroup("fr.insee", "group-empty")).thenReturn(List.of());

        List<PartialCodesList> result = ddiService.getCodeListsByGroup("fr.insee", "group-empty");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(ddiRepository).getLogicalProductsByGroup("fr.insee", "group-empty");
    }

    @Test
    void shouldGetMissingCodesListsByGroup() {
        // Délégation pure au repository (la marche Group → LP → MRS → MMVR → CodeList y est faite).
        List<PartialCodesList> expected =
                List.of(new PartialCodesList("cl-1", "Sentinelles âge", new Date(), "fr.insee"));
        when(ddiRepository.getMissingCodesListsByGroup("fr.insee", "group-1")).thenReturn(expected);

        List<PartialCodesList> result = ddiService.getMissingCodesListsByGroup("fr.insee", "group-1");

        assertEquals(expected, result);
        verify(ddiRepository).getMissingCodesListsByGroup("fr.insee", "group-1");
    }

    @Test
    void shouldGetVariablesUsingCodeList() {
        // Given
        List<CodeListVariableUsage> expected = List.of(new CodeListVariableUsage(
                "fr.insee",
                "su-1",
                "Recensement 2024",
                "fr.insee",
                "pi-1",
                "Fichier détail",
                "fr.insee",
                "var-1",
                "Sexe"));
        when(ddiRepository.getVariablesUsingCodeList("fr.insee", "cl-1")).thenReturn(expected);

        // When
        List<CodeListVariableUsage> result = ddiService.getVariablesUsingCodeList("fr.insee", "cl-1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("var-1", result.get(0).variableId());
        assertEquals("pi-1", result.get(0).physicalInstanceId());

        verify(ddiRepository).getVariablesUsingCodeList("fr.insee", "cl-1");
    }

    @Test
    void shouldGetCodeListsUsingCategory() {
        // Given
        List<CategoryCodeListUsage> expected = List.of(new CategoryCodeListUsage(
                new UsageItem("fr.insee", "grp-1", "Groupe démographie"),
                new UsageItem("fr.insee", "su-1", "Recensement 2024"),
                new UsageItem("fr.insee", "pi-1", "Fichier détail"),
                new UsageItem("fr.insee", "var-1", "Sexe"),
                new UsageItem("fr.insee", "cl-1", "Pays")));
        when(ddiRepository.getCodeListsUsingCategory("fr.insee", "cat-1")).thenReturn(expected);

        // When
        List<CategoryCodeListUsage> result = ddiService.getCodeListsUsingCategory("fr.insee", "cat-1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("cl-1", result.get(0).codeList().id());
        assertEquals("Pays", result.get(0).codeList().label());

        verify(ddiRepository).getCodeListsUsingCategory("fr.insee", "cat-1");
    }

    @Test
    void shouldGetDdi4PhysicalInstance() {
        // Given
        String agencyId = "fr.insee";
        String instanceId = "pi-test";
        Ddi4Response expectedResponse =
                new Ddi4Response("test-schema", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null);
        when(ddiRepository.getPhysicalInstance(agencyId, instanceId)).thenReturn(expectedResponse);

        // When
        Ddi4Response result = ddiService.getDdi4PhysicalInstance(agencyId, instanceId);

        // Then
        assertNotNull(result);
        assertEquals("test-schema", result.schema());

        verify(ddiRepository).getPhysicalInstance(agencyId, instanceId);
    }

    @Test
    void getDdi4PhysicalInstance_sortsVariablesByNameAscending() {
        String agencyId = "fr.insee";
        String instanceId = "pi-1";
        Ddi4Response repoResponse = new Ddi4Response(
                Ddi4Response.SCHEMA,
                List.of(),
                List.of(),
                List.of(),
                List.of(
                        variableWithName("v-c", "charlie"),
                        variableWithName("v-a", "alpha"),
                        variableWithName("v-b", "bravo")),
                List.of(),
                List.of(),
                null);
        when(ddiRepository.getPhysicalInstance(agencyId, instanceId)).thenReturn(repoResponse);

        Ddi4Response result = ddiService.getDdi4PhysicalInstance(agencyId, instanceId);

        assertEquals(
                List.of("alpha", "bravo", "charlie"),
                result.variable().stream()
                        .map(v -> v.variableName().getFirst().value())
                        .toList());
    }

    private Ddi4Variable variableWithName(String id, String name) {
        return new Ddi4Variable(
                Ddi4Variable.TYPE,
                null,
                "urn:ddi:fr.insee:" + id + ":1",
                "fr.insee",
                id,
                "1",
                null,
                LangStrings.of("fr-FR", name),
                null,
                null,
                null,
                null);
    }

    @Test
    void getPhysicalInstanceCodeLists_delegatesToRepository() {
        // Le endpoint /codeslists doit s'appuyer sur une méthode dédiée du repo
        // au lieu de récupérer la PI complète (qui n'inclut plus les CodeList).
        String agencyId = "fr.insee";
        String instanceId = "pi-test";
        List<Ddi4CodeList> expected = List.of(new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                null,
                "urn:ddi:fr.insee:cl-1:1",
                agencyId,
                "cl-1",
                "1",
                LangStrings.of("fr-FR", "ma cl"),
                null,
                List.<Code>of()));
        when(ddiRepository.getPhysicalInstanceCodeLists(agencyId, instanceId)).thenReturn(expected);

        List<Ddi4CodeList> result = ddiService.getPhysicalInstanceCodeLists(agencyId, instanceId);

        assertSame(expected, result);
        verify(ddiRepository).getPhysicalInstanceCodeLists(agencyId, instanceId);
    }

    @Test
    void shouldUpdatePhysicalInstance() {
        // Given
        String agencyId = "fr.insee";
        String instanceId = "test-id";
        UpdatePhysicalInstanceRequest request = new UpdatePhysicalInstanceRequest(
                "Updated Physical Instance Label", "Updated DataRelationship Label", "Updated LogicalRecord Label");
        Ddi4Response expectedResponse = new Ddi4Response(
                "updated-schema", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null);
        when(ddiRepository.getPhysicalInstance(agencyId, instanceId)).thenReturn(expectedResponse);

        // When
        Ddi4Response result = ddiService.updatePhysicalInstance(agencyId, instanceId, request);

        // Then
        assertNotNull(result);
        assertEquals("updated-schema", result.schema());
        verify(ddiRepository).updatePhysicalInstance(agencyId, instanceId, request);
        verify(ddiRepository).getPhysicalInstance(agencyId, instanceId);
    }

    @Test
    void shouldKeepStoredVersionDateForUnchangedItemsOnFullUpdate() {
        // Given : l'état stocké et un payload au même contenu mais avec une autre date
        CogsDate storedDate = CogsDate.ofDateTime("2020-01-01T00:00:00Z");
        Ddi4Response stored = physicalInstanceOnlyResponse(storedDate, "Ma PI");
        Ddi4Response incoming = physicalInstanceOnlyResponse(CogsDate.ofDateTime("2026-01-01T00:00:00Z"), "Ma PI");
        when(ddiRepository.getFullPhysicalInstance("fr.insee", "pi-1")).thenReturn(stored);

        // When
        ddiService.updateFullPhysicalInstance("fr.insee", "pi-1", incoming);

        // Then : GET préalable, puis update avec le payload réconcilié (date stockée)
        InOrder inOrder = inOrder(ddiRepository);
        inOrder.verify(ddiRepository).getFullPhysicalInstance("fr.insee", "pi-1");
        ArgumentCaptor<Ddi4Response> saved = ArgumentCaptor.forClass(Ddi4Response.class);
        inOrder.verify(ddiRepository).updateFullPhysicalInstance(eq("fr.insee"), eq("pi-1"), saved.capture());
        assertEquals(storedDate, saved.getValue().physicalInstance().getFirst().versionDate());
    }

    @Test
    void shouldStampModifiedItemsWithClockNowOnFullUpdate() {
        // Given : le contenu de la PI change (titre)
        Ddi4Response stored = physicalInstanceOnlyResponse(CogsDate.ofDateTime("2020-01-01T00:00:00Z"), "Ma PI");
        Ddi4Response incoming =
                physicalInstanceOnlyResponse(CogsDate.ofDateTime("2020-01-01T00:00:00Z"), "Ma PI modifiée");
        when(ddiRepository.getFullPhysicalInstance("fr.insee", "pi-1")).thenReturn(stored);

        // When
        ddiService.updateFullPhysicalInstance("fr.insee", "pi-1", incoming);

        // Then : date à « maintenant » selon l'horloge injectée
        ArgumentCaptor<Ddi4Response> saved = ArgumentCaptor.forClass(Ddi4Response.class);
        verify(ddiRepository).updateFullPhysicalInstance(eq("fr.insee"), eq("pi-1"), saved.capture());
        assertEquals(
                CogsDate.ofDateTime("2026-08-03T10:00:00+02:00"),
                saved.getValue().physicalInstance().getFirst().versionDate());
    }

    /**
     * Valeurs sentinelles (#1566) : les labels de la MMVR et de sa CodeList de sentinelles sont
     * obligatoires — un payload qui les omet est rejeté avant toute écriture.
     */
    @Test
    void shouldRejectFullUpdateWhenMmvrHasNoLabel() {
        Ddi4ManagedMissingValuesRepresentation mmvrSansLabel = new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                null,
                "urn:ddi:fr.insee:mmvr-1:1",
                "fr.insee",
                "mmvr-1",
                "1",
                null,
                List.of(new CodeRepresentation(
                        CodeRepresentation.TYPE, false, Reference.of("fr.insee", "cl-sent", "1", "CodeList"))));
        Ddi4Response incoming =
                new Ddi4Response(Ddi4Response.SCHEMA, null, null, null, null, null, null, List.of(mmvrSansLabel));

        InvalidSentinelValuesException exception = assertThrows(
                InvalidSentinelValuesException.class,
                () -> ddiService.updateFullPhysicalInstance("fr.insee", "pi-1", incoming));

        assertTrue(exception.getMessage().contains("mmvr-1"));
        verify(ddiRepository, never()).updateFullPhysicalInstance(anyString(), anyString(), any());
    }

    @Test
    void shouldRejectFullUpdateWhenSentinelCodeListHasNoLabel() {
        Ddi4ManagedMissingValuesRepresentation mmvr = new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                null,
                "urn:ddi:fr.insee:mmvr-1:1",
                "fr.insee",
                "mmvr-1",
                "1",
                LangStrings.of("fr-FR", "Sentinelles"),
                List.of(new CodeRepresentation(
                        CodeRepresentation.TYPE, false, Reference.of("fr.insee", "cl-sent", "1", "CodeList"))));
        // La CodeList de sentinelles référencée par la MMVR est dans le payload, sans label.
        Ddi4CodeList sentinelCodeListSansLabel = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                null,
                "urn:ddi:fr.insee:cl-sent:1",
                "fr.insee",
                "cl-sent",
                "1",
                null,
                null,
                List.of());
        Ddi4Response incoming = new Ddi4Response(
                Ddi4Response.SCHEMA, null, null, null, null, List.of(sentinelCodeListSansLabel), null, List.of(mmvr));

        InvalidSentinelValuesException exception = assertThrows(
                InvalidSentinelValuesException.class,
                () -> ddiService.updateFullPhysicalInstance("fr.insee", "pi-1", incoming));

        assertTrue(exception.getMessage().contains("cl-sent"));
        verify(ddiRepository, never()).updateFullPhysicalInstance(anyString(), anyString(), any());
    }

    @Test
    void shouldAcceptFullUpdateWhenSentinelLabelsArePresent() {
        Ddi4ManagedMissingValuesRepresentation mmvr = new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                null,
                "urn:ddi:fr.insee:mmvr-1:1",
                "fr.insee",
                "mmvr-1",
                "1",
                LangStrings.of("fr-FR", "Sentinelles"),
                List.of(new CodeRepresentation(
                        CodeRepresentation.TYPE, false, Reference.of("fr.insee", "cl-sent", "1", "CodeList"))));
        Ddi4CodeList sentinelCodeList = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                null,
                "urn:ddi:fr.insee:cl-sent:1",
                "fr.insee",
                "cl-sent",
                "1",
                LangStrings.of("fr-FR", "Sentinelles"),
                null,
                List.of());
        Ddi4Response incoming = new Ddi4Response(
                Ddi4Response.SCHEMA, null, null, null, null, List.of(sentinelCodeList), null, List.of(mmvr));
        when(ddiRepository.getFullPhysicalInstance("fr.insee", "pi-1")).thenReturn(null);

        ddiService.updateFullPhysicalInstance("fr.insee", "pi-1", incoming);

        verify(ddiRepository).updateFullPhysicalInstance(eq("fr.insee"), eq("pi-1"), any());
    }

    private static Ddi4Response physicalInstanceOnlyResponse(CogsDate date, String title) {
        Ddi4PhysicalInstance physicalInstance = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                date,
                Reference.synthesizeUrn("fr.insee", "pi-1", "1"),
                "fr.insee",
                "pi-1",
                "1",
                null,
                new Citation(List.of(new LangString("fr", title))),
                null);
        return new Ddi4Response(Ddi4Response.SCHEMA, null, List.of(physicalInstance), null, null, null, null, null);
    }

    @Test
    void shouldCreatePhysicalInstance() {
        // Given
        CreatePhysicalInstanceRequest request = new CreatePhysicalInstanceRequest(
                "New Physical Instance Label",
                "New DataRelationship Label",
                "New LogicalRecord Label",
                null,
                null,
                null,
                null);
        Ddi4Response expectedResponse =
                new Ddi4Response("new-schema", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null);
        when(ddiRepository.createPhysicalInstance(request)).thenReturn(expectedResponse);

        // When
        Ddi4Response result = ddiService.createPhysicalInstance(request);

        // Then
        assertNotNull(result);
        assertEquals("new-schema", result.schema());
        verify(ddiRepository).createPhysicalInstance(request);
    }

    @Test
    void shouldGetGroups() {
        // Given
        List<PartialGroup> expectedGroups = List.of(
                new PartialGroup("group-1", "Base permanente des équipements", new Date(), "fr.insee", List.of()),
                new PartialGroup("group-2", "Recensement de la population", new Date(), "fr.insee", List.of()));
        when(ddiRepository.getGroups()).thenReturn(expectedGroups);

        // When
        List<PartialGroup> result = ddiService.getGroups();

        // Then : tri par label en ordre croissant (A-Z)
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("group-1", result.get(0).id());
        assertEquals("Base permanente des équipements", result.get(0).label());
        assertEquals("group-2", result.get(1).id());
        assertEquals("Recensement de la population", result.get(1).label());

        verify(ddiRepository).getGroups();
    }

    @Test
    void getGroups_shouldBeSortedByLabelAscending() {
        when(ddiRepository.getGroups())
                .thenReturn(List.of(
                        new PartialGroup("g-a", "alpha", new Date(), "fr.insee", List.of()),
                        new PartialGroup("g-c", "Charlie", new Date(), "fr.insee", List.of()),
                        new PartialGroup("g-b", "Bravo", new Date(), "fr.insee", List.of())));

        List<PartialGroup> result = ddiService.getGroups();

        assertEquals(
                List.of("alpha", "Bravo", "Charlie"),
                result.stream().map(PartialGroup::label).toList());
    }

    @Test
    void getGroupsFilteredByStamp_shouldBeSortedByLabelAscending() {
        String iri = "http://id.insee.fr/operations/serie/s1001";
        when(ddiRepository.getGroups())
                .thenReturn(List.of(
                        new PartialGroup("g-c", "Charlie", null, "fr.insee", List.of(iri)),
                        new PartialGroup("g-a", "alpha", null, "fr.insee", List.of(iri))));
        when(seriesCreatorsPort.getCreatorsForSeries(Set.of(iri))).thenReturn(Map.of(iri, List.of("stamp-A")));

        List<PartialGroup> result = ddiService.getGroupsFilteredByStamp(Set.of("stamp-A"));

        assertEquals(
                List.of("alpha", "Charlie"),
                result.stream().map(PartialGroup::label).toList());
    }

    @Test
    void shouldGetDdi4Group() {
        // Given
        String agencyId = "fr.insee";
        String groupId = "10a689ce-7006-429b-8e84-036b7787b422";

        Citation citation = new Citation(LangStrings.of("fr-FR", "Base permanente des équipements"));
        Reference suRef1 = Reference.of(agencyId, "su-1", "1", "StudyUnit");
        Reference suRef2 = Reference.of(agencyId, "su-2", "1", "StudyUnit");

        Ddi4Group group = new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
                "urn:ddi:fr.insee:" + groupId + ":1",
                agencyId,
                groupId,
                "1",
                "bauhaus",
                citation,
                List.of(suRef1, suRef2),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries");

        Ddi4StudyUnit studyUnit1 = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
                "urn:ddi:fr.insee:su-1:1",
                agencyId,
                "su-1",
                "1",
                new Citation(LangStrings.of("fr-FR", "BPE 2021")),
                "http://id.insee.fr/operations/operation/op1",
                null);

        Ddi4StudyUnit studyUnit2 = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
                "urn:ddi:fr.insee:su-2:1",
                agencyId,
                "su-2",
                "1",
                new Citation(LangStrings.of("fr-FR", "BPE 2022")),
                "http://id.insee.fr/operations/operation/op2",
                null);

        Reference topLevelRef = Reference.of(agencyId, groupId, "1", "Group");

        Ddi4GroupResponse expectedResponse =
                new Ddi4GroupResponse("ddi:4.0", List.of(topLevelRef), List.of(group), List.of(studyUnit1, studyUnit2));

        when(ddiRepository.getGroup(agencyId, groupId)).thenReturn(expectedResponse);

        // When
        Ddi4GroupResponse result = ddiService.getDdi4Group(agencyId, groupId);

        // Then
        assertNotNull(result);
        assertEquals("ddi:4.0", result.schema());
        assertEquals(1, result.group().size());
        assertEquals(groupId, result.group().get(0).id());
        assertEquals(
                "Base permanente des équipements",
                result.group().get(0).citation().title().get(0).value());
        assertEquals(2, result.studyUnit().size());
        // tri par label décroissant (Z-A) : 2022 avant 2021
        assertEquals(
                "BPE 2022", result.studyUnit().get(0).citation().title().get(0).value());
        assertEquals(
                "BPE 2021", result.studyUnit().get(1).citation().title().get(0).value());

        verify(ddiRepository).getGroup(agencyId, groupId);
    }

    @Test
    void getDdi4Group_shouldSortStudyUnitsByLabelDescending() {
        String agencyId = "fr.insee";
        String groupId = "g1";
        Ddi4StudyUnit su2012 = studyUnitWithTitle("su-2012", "Enquête innovation 2012");
        Ddi4StudyUnit su2014 = studyUnitWithTitle("su-2014", "Enquête innovation 2014");
        Ddi4StudyUnit su2010 = studyUnitWithTitle("su-2010", "Enquête innovation 2010");
        when(ddiRepository.getGroup(agencyId, groupId))
                .thenReturn(new Ddi4GroupResponse("ddi:4.0", List.of(), List.of(), List.of(su2012, su2014, su2010)));

        Ddi4GroupResponse result = ddiService.getDdi4Group(agencyId, groupId);

        assertEquals(
                List.of("Enquête innovation 2014", "Enquête innovation 2012", "Enquête innovation 2010"),
                result.studyUnit().stream()
                        .map(su -> su.citation().title().get(0).value())
                        .toList());
    }

    private Ddi4StudyUnit studyUnitWithTitle(String id, String title) {
        return new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
                "urn:ddi:fr.insee:" + id + ":1",
                "fr.insee",
                id,
                "1",
                new Citation(LangStrings.of("fr-FR", title)),
                "http://id.insee.fr/operations/operation/op",
                null);
    }

    @Test
    void shouldGetMutualizedCodesLists() {
        // Given
        List<PartialCodesList> expectedCodesLists = List.of(
                new PartialCodesList(
                        "fc65a527-a04b-4505-85de-0a181e54dbad",
                        "NAF rév. 2, 2008 - Niveau 5 - Sous-classes",
                        new Date(),
                        "fr.insee"),
                new PartialCodesList("another-id", "Another Code List", new Date(), "fr.insee"));
        when(ddiRepository.getMutualizedCodesLists()).thenReturn(expectedCodesLists);

        // When
        List<PartialCodesList> result = ddiService.getMutualizedCodesLists();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("fc65a527-a04b-4505-85de-0a181e54dbad", result.get(0).id());
        assertEquals("NAF rév. 2, 2008 - Niveau 5 - Sous-classes", result.get(0).label());
        assertEquals("fr.insee", result.get(0).agency());
        assertEquals("another-id", result.get(1).id());
        assertEquals("Another Code List", result.get(1).label());

        verify(ddiRepository).getMutualizedCodesLists();
    }

    @Test
    void evictMutualizedCodesListsCache_delegatesToRepository() {
        ddiService.evictMutualizedCodesListsCache();

        verify(ddiRepository).evictMutualizedCodesListsCache();
    }

    @Test
    void shouldGetMutualizedCodesListDelegatingToRepository() {
        // Given
        String agencyId = "fr.insee";
        String id = "fc65a527-a04b-4505-85de-0a181e54dbad";
        Ddi4Response expectedResponse =
                new Ddi4Response("ddi:4.0", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null);
        when(ddiRepository.getMutualizedCodesList(agencyId, id)).thenReturn(expectedResponse);

        // When
        Ddi4Response result = ddiService.getMutualizedCodesList(agencyId, id);

        // Then
        assertSame(expectedResponse, result);
        verify(ddiRepository).getMutualizedCodesList(agencyId, id);
    }

    @Test
    void shouldGetItemXmlWithVersion() {
        String agency = "fr.insee";
        String id = "c05c0443-fc56-4069-9bea-a9c7300ae0a0";
        String version = "1";
        String expectedXml = "<Fragment><PhysicalInstance/></Fragment>";
        when(ddiRepository.getItemXml(agency, id, version)).thenReturn(expectedXml);

        String result = ddiService.getItemXml(agency, id, version);

        assertEquals(expectedXml, result);
        verify(ddiRepository).getItemXml(agency, id, version);
    }

    @Test
    void shouldGetItemXmlLatestVersion() {
        String agency = "fr.insee";
        String id = "c05c0443-fc56-4069-9bea-a9c7300ae0a0";
        String expectedXml = "<Fragment><PhysicalInstance/></Fragment>";
        when(ddiRepository.getItemXml(agency, id)).thenReturn(expectedXml);

        String result = ddiService.getItemXml(agency, id);

        assertEquals(expectedXml, result);
        verify(ddiRepository).getItemXml(agency, id);
    }

    @Test
    void shouldGetPhysicalInstanceParents() {
        String agencyId = "fr.insee";
        String id = "pi-123";
        PhysicalInstanceParents expected = new PhysicalInstanceParents("fr.insee", "su-456", "fr.insee", "grp-789");
        when(ddiRepository.getPhysicalInstanceParents(agencyId, id)).thenReturn(expected);

        PhysicalInstanceParents result = ddiService.getPhysicalInstanceParents(agencyId, id);

        assertNotNull(result);
        assertEquals("su-456", result.studyUnitId());
        assertEquals("grp-789", result.groupId());
        verify(ddiRepository).getPhysicalInstanceParents(agencyId, id);
    }

    @Test
    void shouldGetPhysicalInstanceParents_resolvesCreatorStampsOfParentGroup() {
        String agencyId = "fr.insee";
        String id = "pi-123";
        String seriesIri = "http://id.insee.fr/operations/serie/s1001";

        when(ddiRepository.getPhysicalInstanceParents(agencyId, id))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-456", "fr.insee", "grp-789"));

        Ddi4Group group = new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
                "urn:ddi:fr.insee:grp-789:1",
                "fr.insee",
                "grp-789",
                "1",
                "bauhaus",
                null,
                List.of(),
                List.of(seriesIri),
                "insee:StatisticalOperationSeries");
        when(ddiRepository.getGroup("fr.insee", "grp-789"))
                .thenReturn(new Ddi4GroupResponse("ddi:4.0", List.of(), List.of(group), List.of()));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(seriesIri)))
                .thenReturn(Map.of(seriesIri, List.of("stamp-A", "stamp-B")));

        PhysicalInstanceParents result = ddiService.getPhysicalInstanceParents(agencyId, id);

        assertNotNull(result);
        assertEquals("grp-789", result.groupId());
        assertEquals(Set.of("stamp-A", "stamp-B"), Set.copyOf(result.stamps()));
    }

    @Test
    void shouldGetPhysicalInstanceParents_resolvesParentGroupLabel() {
        String agencyId = "fr.insee";
        String id = "pi-123";

        when(ddiRepository.getPhysicalInstanceParents(agencyId, id))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-456", "fr.insee", "grp-789"));

        Ddi4Group group = new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
                "urn:ddi:fr.insee:grp-789:1",
                "fr.insee",
                "grp-789",
                "1",
                "bauhaus",
                new Citation(LangStrings.of("fr-FR", "Base permanente des équipements")),
                null,
                List.of(),
                "insee:StatisticalOperationSeries");
        when(ddiRepository.getGroup("fr.insee", "grp-789"))
                .thenReturn(new Ddi4GroupResponse("ddi:4.0", List.of(), List.of(group), List.of()));

        PhysicalInstanceParents result = ddiService.getPhysicalInstanceParents(agencyId, id);

        assertEquals("Base permanente des équipements", result.groupLabel());
    }

    @Test
    void shouldGetPhysicalInstanceParents_resolvesStudyUnitLabel() {
        String agencyId = "fr.insee";
        String id = "pi-123";

        when(ddiRepository.getPhysicalInstanceParents(agencyId, id))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-456", "fr.insee", "grp-789"));

        Ddi4Group group = new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
                "urn:ddi:fr.insee:grp-789:1",
                "fr.insee",
                "grp-789",
                "1",
                "bauhaus",
                null,
                null,
                List.of(),
                "insee:StatisticalOperationSeries");
        // Le groupe parent files ses study units ; on retrouve le label de l'étude rattachée
        // à la PI (su-456) dans cette même liste, sans appel Colectica supplémentaire.
        when(ddiRepository.getGroup("fr.insee", "grp-789"))
                .thenReturn(new Ddi4GroupResponse(
                        "ddi:4.0",
                        List.of(),
                        List.of(group),
                        List.of(
                                studyUnitWithTitle("su-000", "Autre enquête"),
                                studyUnitWithTitle("su-456", "Enquête emploi 2024"))));

        PhysicalInstanceParents result = ddiService.getPhysicalInstanceParents(agencyId, id);

        assertEquals("Enquête emploi 2024", result.studyUnitLabel());
    }

    @Test
    void shouldGetPhysicalInstancesFilteredByStamp_keepsOnlyInstancesOfUserGroups() {
        PartialPhysicalInstance pi1 = new PartialPhysicalInstance("pi-1", "PI 1", new Date(), "fr.insee");
        PartialPhysicalInstance pi2 = new PartialPhysicalInstance("pi-2", "PI 2", new Date(), "fr.insee");
        when(ddiRepository.getPhysicalInstancesViaAdvancedQuery()).thenReturn(List.of(pi1, pi2));

        when(ddiRepository.getPhysicalInstanceParents("fr.insee", "pi-1"))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-1", "fr.insee", "g1"));
        when(ddiRepository.getPhysicalInstanceParents("fr.insee", "pi-2"))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-2", "fr.insee", "g2"));

        String iri1 = "http://id.insee.fr/operations/serie/s1";
        String iri2 = "http://id.insee.fr/operations/serie/s2";
        when(ddiRepository.getGroup("fr.insee", "g1")).thenReturn(groupResponseWithSeries("g1", iri1));
        when(ddiRepository.getGroup("fr.insee", "g2")).thenReturn(groupResponseWithSeries("g2", iri2));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri1))).thenReturn(Map.of(iri1, List.of("stamp-A")));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri2))).thenReturn(Map.of(iri2, List.of("stamp-B")));

        List<PartialPhysicalInstance> result = ddiService.getPhysicalInstancesFilteredByStamp(Set.of("stamp-A"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("pi-1", result.getFirst().id());
    }

    @Test
    void shouldGetPhysicalInstancesFilteredByStamp_resolvesGroupStampsOncePerGroup() {
        PartialPhysicalInstance pi1 = new PartialPhysicalInstance("pi-1", "PI 1", new Date(), "fr.insee");
        PartialPhysicalInstance pi2 = new PartialPhysicalInstance("pi-2", "PI 2", new Date(), "fr.insee");
        when(ddiRepository.getPhysicalInstancesViaAdvancedQuery()).thenReturn(List.of(pi1, pi2));

        // les deux PI partagent le même groupe parent g1
        when(ddiRepository.getPhysicalInstanceParents("fr.insee", "pi-1"))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-1", "fr.insee", "g1"));
        when(ddiRepository.getPhysicalInstanceParents("fr.insee", "pi-2"))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-2", "fr.insee", "g1"));

        String iri1 = "http://id.insee.fr/operations/serie/s1";
        when(ddiRepository.getGroup("fr.insee", "g1")).thenReturn(groupResponseWithSeries("g1", iri1));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri1))).thenReturn(Map.of(iri1, List.of("stamp-A")));

        List<PartialPhysicalInstance> result = ddiService.getPhysicalInstancesFilteredByStamp(Set.of("stamp-A"));

        assertEquals(2, result.size());
        // les stamps du groupe partagé ne sont résolus qu'une seule fois
        verify(ddiRepository, times(1)).getGroup("fr.insee", "g1");
    }

    private Ddi4GroupResponse groupResponseWithSeries(String groupId, String... seriesIris) {
        Ddi4Group group = new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
                "urn:ddi:fr.insee:" + groupId + ":1",
                "fr.insee",
                groupId,
                "1",
                "bauhaus",
                null,
                List.of(),
                List.of(seriesIris),
                "insee:StatisticalOperationSeries");
        return new Ddi4GroupResponse("ddi:4.0", List.of(), List.of(group), List.of());
    }

    @Test
    void shouldReturnNullWhenItemXmlNotFound() {
        when(ddiRepository.getItemXml("fr.insee", "unknown-id", "1")).thenReturn(null);

        String result = ddiService.getItemXml("fr.insee", "unknown-id", "1");

        assertNull(result);
    }

    @Test
    void shouldGetGroupsFilteredByStamp_returnsMatchingGroups() {
        String iri1 = "http://id.insee.fr/operations/serie/s1001";
        String iri2 = "http://id.insee.fr/operations/serie/s1002";

        List<PartialGroup> allGroups = List.of(
                new PartialGroup("g1", "Group 1", null, "fr.insee", List.of(iri1)),
                new PartialGroup("g2", "Group 2", null, "fr.insee", List.of(iri2)));
        when(ddiRepository.getGroups()).thenReturn(allGroups);
        when(seriesCreatorsPort.getCreatorsForSeries(Set.of(iri1, iri2))).thenReturn(Map.of(iri1, List.of("stamp-A")));

        List<PartialGroup> result = ddiService.getGroupsFilteredByStamp(Set.of("stamp-A"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("g1", result.getFirst().id());
    }

    @Test
    void shouldGetStudyUnitXmlByOperationIri_returnsXmlWhenFound() {
        String operationIri = "http://id.insee.fr/operations/operation/op1";
        String expectedXml = "<StudyUnit><r:UserID>http://id.insee.fr/operations/operation/op1</r:UserID></StudyUnit>";
        when(ddiRepository.findStudyUnitXmlByOperationIri(operationIri)).thenReturn(Optional.of(expectedXml));

        Optional<String> result = ddiService.getStudyUnitXmlByOperationIri(operationIri);

        assertTrue(result.isPresent());
        assertEquals(expectedXml, result.get());
        verify(ddiRepository).findStudyUnitXmlByOperationIri(operationIri);
    }

    @Test
    void shouldGetStudyUnitXmlByOperationIri_returnsEmptyWhenNotFound() {
        String operationIri = "http://id.insee.fr/operations/operation/unknown";
        when(ddiRepository.findStudyUnitXmlByOperationIri(operationIri)).thenReturn(Optional.empty());

        Optional<String> result = ddiService.getStudyUnitXmlByOperationIri(operationIri);

        assertFalse(result.isPresent());
        verify(ddiRepository).findStudyUnitXmlByOperationIri(operationIri);
    }

    @Test
    void shouldGetStudyUnitByOperationIri_returnsDdi4WhenFound() {
        String operationIri = "http://id.insee.fr/operations/operation/op1";
        Ddi4StudyUnitResponse expected =
                new Ddi4StudyUnitResponse(Ddi4Response.SCHEMA, List.of(), List.of(), List.of());
        when(ddiRepository.findStudyUnitByOperationIri(operationIri)).thenReturn(Optional.of(expected));

        Optional<Ddi4StudyUnitResponse> result = ddiService.getStudyUnitByOperationIri(operationIri);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
        verify(ddiRepository).findStudyUnitByOperationIri(operationIri);
    }

    @Test
    void shouldGetStudyUnitByOperationIri_returnsEmptyWhenNotFound() {
        String operationIri = "http://id.insee.fr/operations/operation/unknown";
        when(ddiRepository.findStudyUnitByOperationIri(operationIri)).thenReturn(Optional.empty());

        assertFalse(ddiService.getStudyUnitByOperationIri(operationIri).isPresent());
        verify(ddiRepository).findStudyUnitByOperationIri(operationIri);
    }

    @Test
    void shouldGetGroupsFilteredByStamp_returnsAllGroups_whenAdmin() {
        String iri1 = "http://id.insee.fr/operations/serie/s1001";
        String iri2 = "http://id.insee.fr/operations/serie/s1002";

        List<PartialGroup> allGroups = List.of(
                new PartialGroup("g1", "Group 1", null, "fr.insee", List.of(iri1)),
                new PartialGroup("g2", "Group 2", null, "fr.insee", List.of(iri2)));
        when(ddiRepository.getGroups()).thenReturn(allGroups);
        when(seriesCreatorsPort.getCreatorsForSeries(Set.of(iri1, iri2)))
                .thenReturn(Map.of(iri1, List.of("stamp-A"), iri2, List.of("stamp-B")));

        List<PartialGroup> result = ddiService.getGroupsFilteredByStamp(Set.of("stamp-A", "stamp-B"));

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Le GET de référence exclut volontairement les CodeList/Category du payload : si la
     * réconciliation s'appuyait sur lui seul, toute liste de codes accompagnant le PUT passerait
     * pour nouvelle et redaterait, par propagation, les variables qui la référencent — y compris
     * celles que l'utilisateur n'a pas touchées.
     */
    @Test
    void shouldKeepStoredVersionDateOfUntouchedVariableWhenItsCodeListIsPartOfThePayload() {
        // Given : une variable code inchangée, dont la liste de codes (inchangée elle aussi)
        // accompagne le payload parce qu'une autre variable l'a chargée.
        CogsDate storedDate = CogsDate.ofDateTime("2020-01-01T00:00:00Z");
        Ddi4Variable variable = codeVariable(storedDate);
        Ddi4CodeList codeList = sharedCodeList(storedDate);

        when(ddiRepository.getFullPhysicalInstance("fr.insee", "pi-1"))
                .thenReturn(new Ddi4Response(
                        Ddi4Response.SCHEMA, null, null, null, List.of(variable), List.of(codeList), null, null));

        Ddi4Response incoming = new Ddi4Response(
                Ddi4Response.SCHEMA, null, null, null, List.of(variable), List.of(codeList), null, null);

        // When
        ddiService.updateFullPhysicalInstance("fr.insee", "pi-1", incoming);

        // Then
        ArgumentCaptor<Ddi4Response> saved = ArgumentCaptor.forClass(Ddi4Response.class);
        verify(ddiRepository).updateFullPhysicalInstance(eq("fr.insee"), eq("pi-1"), saved.capture());
        assertEquals(storedDate, saved.getValue().variable().getFirst().versionDate());
    }

    private static Ddi4Variable codeVariable(CogsDate date) {
        return new Ddi4Variable(
                Ddi4Variable.TYPE,
                date,
                Reference.synthesizeUrn("fr.insee", "var-1", "1"),
                "fr.insee",
                "var-1",
                "1",
                null,
                List.of(new LangString("fr", "VAR1")),
                List.of(new LangString("fr", "Variable 1")),
                null,
                new VariableRepresentation(
                        null,
                        new CodeRepresentation(
                                CodeRepresentation.TYPE,
                                null,
                                Reference.of("fr.insee", "cl-1", "1", Ddi4CodeList.TYPE)),
                        null,
                        null,
                        null,
                        null),
                null);
    }

    private static Ddi4CodeList sharedCodeList(CogsDate date) {
        return new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                date,
                Reference.synthesizeUrn("fr.insee", "cl-1", "1"),
                "fr.insee",
                "cl-1",
                "1",
                List.of(new LangString("fr", "Liste 1")),
                null,
                null);
    }
}
