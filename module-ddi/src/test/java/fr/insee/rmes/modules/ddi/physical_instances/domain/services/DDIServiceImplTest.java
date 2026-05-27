package fr.insee.rmes.modules.ddi.physical_instances.domain.services;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DDIServiceImplTest {

    @Mock
    private DDIRepository ddiRepository;

    @Mock
    private SeriesCreatorsPort seriesCreatorsPort;

    private DDIServiceImpl ddiService;

    @BeforeEach
    void setUp() {
        ddiService = new DDIServiceImpl(ddiRepository, seriesCreatorsPort);
    }

    @Test
    void shouldGetPhysicalInstances() {
        // Given
        List<PartialPhysicalInstance> expectedInstances = List.of(
                new PartialPhysicalInstance("pi-1", "Physical Instance 1", new Date(), "fr.insee"),
                new PartialPhysicalInstance("pi-2", "Physical Instance 2", new Date(), "fr.insee"),
                new PartialPhysicalInstance("pi-3", "Physical Instance 3", new Date(), "fr.insee")
        );
        when(ddiRepository.getPhysicalInstances()).thenReturn(expectedInstances);

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

        verify(ddiRepository).getPhysicalInstances();
    }

    @Test
    void shouldGetDdi4PhysicalInstance() {
        // Given
        String agencyId = "fr.insee";
        String instanceId = "pi-test";
        Ddi4Response expectedResponse = new Ddi4Response(
            "test-schema",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );
        when(ddiRepository.getPhysicalInstance(agencyId, instanceId)).thenReturn(expectedResponse);

        // When
        Ddi4Response result = ddiService.getDdi4PhysicalInstance(agencyId, instanceId);

        // Then
        assertNotNull(result);
        assertEquals("test-schema", result.schema());

        verify(ddiRepository).getPhysicalInstance(agencyId, instanceId);
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
                agencyId, "cl-1", "1",
                LangStrings.of("fr-FR", "ma cl"),
                List.<Code>of()
        ));
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
            "Updated Physical Instance Label",
            "Updated DataRelationship Label",
            "Updated LogicalRecord Label"
        );
        Ddi4Response expectedResponse = new Ddi4Response(
            "updated-schema",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );
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
    void shouldCreatePhysicalInstance() {
        // Given
        CreatePhysicalInstanceRequest request = new CreatePhysicalInstanceRequest(
            "New Physical Instance Label",
            "New DataRelationship Label",
            "New LogicalRecord Label",
            null, null, null, null
        );
        Ddi4Response expectedResponse = new Ddi4Response(
            "new-schema",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );
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
                new PartialGroup("group-2", "Recensement de la population", new Date(), "fr.insee", List.of())
        );
        when(ddiRepository.getGroups()).thenReturn(expectedGroups);

        // When
        List<PartialGroup> result = ddiService.getGroups();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("group-1", result.get(0).id());
        assertEquals("Base permanente des équipements", result.get(0).label());
        assertEquals("group-2", result.get(1).id());
        assertEquals("Recensement de la population", result.get(1).label());

        verify(ddiRepository).getGroups();
    }

    @Test
    void shouldGetDdi4Group() {
        // Given
        String agencyId = "fr.insee";
        String groupId = "10a689ce-7006-429b-8e84-036b7787b422";

        Citation citation = new Citation(LangStrings.of("fr-FR", "Base permanente des équipements"));
        Reference suRef1 = Reference.of(agencyId, "su-1", "1", "StudyUnit");
        Reference suRef2 = Reference.of(agencyId, "su-2", "1", "StudyUnit");

        Ddi4Group group = new Ddi4Group(Ddi4Group.TYPE,
            CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
            "urn:ddi:fr.insee:" + groupId + ":1",
            agencyId, groupId, "1",
            "bauhaus", citation, List.of(suRef1, suRef2),
            List.of("http://id.insee.fr/operations/serie/s1001"),
            "insee:StatisticalOperationSeries"
        );

        Ddi4StudyUnit studyUnit1 = new Ddi4StudyUnit(Ddi4StudyUnit.TYPE,
            CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
            "urn:ddi:fr.insee:su-1:1",
            agencyId, "su-1", "1",
            new Citation(LangStrings.of("fr-FR", "BPE 2021")),
            "http://id.insee.fr/operations/operation/op1",
            null
        );

        Ddi4StudyUnit studyUnit2 = new Ddi4StudyUnit(Ddi4StudyUnit.TYPE,
            CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
            "urn:ddi:fr.insee:su-2:1",
            agencyId, "su-2", "1",
            new Citation(LangStrings.of("fr-FR", "BPE 2022")),
            "http://id.insee.fr/operations/operation/op2",
            null
        );

        Reference topLevelRef = Reference.of(agencyId, groupId, "1", "Group");

        Ddi4GroupResponse expectedResponse = new Ddi4GroupResponse(
            "ddi:4.0",
            List.of(topLevelRef),
            List.of(group),
            List.of(studyUnit1, studyUnit2)
        );

        when(ddiRepository.getGroup(agencyId, groupId)).thenReturn(expectedResponse);

        // When
        Ddi4GroupResponse result = ddiService.getDdi4Group(agencyId, groupId);

        // Then
        assertNotNull(result);
        assertEquals("ddi:4.0", result.schema());
        assertEquals(1, result.group().size());
        assertEquals(groupId, result.group().get(0).id());
        assertEquals("Base permanente des équipements", result.group().get(0).citation().title().get(0).value());
        assertEquals(2, result.studyUnit().size());
        assertEquals("BPE 2021", result.studyUnit().get(0).citation().title().get(0).value());
        assertEquals("BPE 2022", result.studyUnit().get(1).citation().title().get(0).value());

        verify(ddiRepository).getGroup(agencyId, groupId);
    }

    @Test
    void shouldGetMutualizedCodesLists() {
        // Given
        List<PartialCodesList> expectedCodesLists = List.of(
                new PartialCodesList("fc65a527-a04b-4505-85de-0a181e54dbad", "NAF rév. 2, 2008 - Niveau 5 - Sous-classes", new Date(), "fr.insee"),
                new PartialCodesList("another-id", "Another Code List", new Date(), "fr.insee")
        );
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
    void shouldGetMutualizedCodesListDelegatingToRepository() {
        // Given
        String agencyId = "fr.insee";
        String id = "fc65a527-a04b-4505-85de-0a181e54dbad";
        Ddi4Response expectedResponse = new Ddi4Response(
                "ddi:4.0", List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
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

        Ddi4Group group = new Ddi4Group(Ddi4Group.TYPE,
                CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
                "urn:ddi:fr.insee:grp-789:1",
                "fr.insee", "grp-789", "1",
                "bauhaus", null, List.of(),
                List.of(seriesIri),
                "insee:StatisticalOperationSeries"
        );
        when(ddiRepository.getGroup("fr.insee", "grp-789")).thenReturn(
                new Ddi4GroupResponse("ddi:4.0", List.of(), List.of(group), List.of()));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(seriesIri)))
                .thenReturn(Map.of(seriesIri, List.of("stamp-A", "stamp-B")));

        PhysicalInstanceParents result = ddiService.getPhysicalInstanceParents(agencyId, id);

        assertNotNull(result);
        assertEquals("grp-789", result.groupId());
        assertEquals(Set.of("stamp-A", "stamp-B"), Set.copyOf(result.stamps()));
    }

    @Test
    void shouldGetPhysicalInstancesFilteredByStamp_keepsOnlyInstancesOfUserGroups() {
        PartialPhysicalInstance pi1 = new PartialPhysicalInstance("pi-1", "PI 1", new Date(), "fr.insee");
        PartialPhysicalInstance pi2 = new PartialPhysicalInstance("pi-2", "PI 2", new Date(), "fr.insee");
        when(ddiRepository.getPhysicalInstances()).thenReturn(List.of(pi1, pi2));

        when(ddiRepository.getPhysicalInstanceParents("fr.insee", "pi-1"))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-1", "fr.insee", "g1"));
        when(ddiRepository.getPhysicalInstanceParents("fr.insee", "pi-2"))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-2", "fr.insee", "g2"));

        String iri1 = "http://id.insee.fr/operations/serie/s1";
        String iri2 = "http://id.insee.fr/operations/serie/s2";
        when(ddiRepository.getGroup("fr.insee", "g1")).thenReturn(groupResponseWithSeries("g1", iri1));
        when(ddiRepository.getGroup("fr.insee", "g2")).thenReturn(groupResponseWithSeries("g2", iri2));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri1)))
                .thenReturn(Map.of(iri1, List.of("stamp-A")));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri2)))
                .thenReturn(Map.of(iri2, List.of("stamp-B")));

        List<PartialPhysicalInstance> result =
                ddiService.getPhysicalInstancesFilteredByStamp(Set.of("stamp-A"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("pi-1", result.getFirst().id());
    }

    @Test
    void shouldGetPhysicalInstancesFilteredByStamp_resolvesGroupStampsOncePerGroup() {
        PartialPhysicalInstance pi1 = new PartialPhysicalInstance("pi-1", "PI 1", new Date(), "fr.insee");
        PartialPhysicalInstance pi2 = new PartialPhysicalInstance("pi-2", "PI 2", new Date(), "fr.insee");
        when(ddiRepository.getPhysicalInstances()).thenReturn(List.of(pi1, pi2));

        // les deux PI partagent le même groupe parent g1
        when(ddiRepository.getPhysicalInstanceParents("fr.insee", "pi-1"))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-1", "fr.insee", "g1"));
        when(ddiRepository.getPhysicalInstanceParents("fr.insee", "pi-2"))
                .thenReturn(new PhysicalInstanceParents("fr.insee", "su-2", "fr.insee", "g1"));

        String iri1 = "http://id.insee.fr/operations/serie/s1";
        when(ddiRepository.getGroup("fr.insee", "g1")).thenReturn(groupResponseWithSeries("g1", iri1));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri1)))
                .thenReturn(Map.of(iri1, List.of("stamp-A")));

        List<PartialPhysicalInstance> result =
                ddiService.getPhysicalInstancesFilteredByStamp(Set.of("stamp-A"));

        assertEquals(2, result.size());
        // les stamps du groupe partagé ne sont résolus qu'une seule fois
        verify(ddiRepository, times(1)).getGroup("fr.insee", "g1");
    }

    private Ddi4GroupResponse groupResponseWithSeries(String groupId, String... seriesIris) {
        Ddi4Group group = new Ddi4Group(Ddi4Group.TYPE,
                CogsDate.ofDateTime("2025-01-09T09:00:00Z"),
                "urn:ddi:fr.insee:" + groupId + ":1",
                "fr.insee", groupId, "1",
                "bauhaus", null, List.of(),
                List.of(seriesIris),
                "insee:StatisticalOperationSeries"
        );
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
                new PartialGroup("g2", "Group 2", null, "fr.insee", List.of(iri2))
        );
        when(ddiRepository.getGroups()).thenReturn(allGroups);
        when(seriesCreatorsPort.getCreatorsForSeries(Set.of(iri1, iri2)))
                .thenReturn(Map.of(iri1, List.of("stamp-A")));

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
    void shouldGetGroupsFilteredByStamp_returnsAllGroups_whenAdmin() {
        String iri1 = "http://id.insee.fr/operations/serie/s1001";
        String iri2 = "http://id.insee.fr/operations/serie/s1002";

        List<PartialGroup> allGroups = List.of(
                new PartialGroup("g1", "Group 1", null, "fr.insee", List.of(iri1)),
                new PartialGroup("g2", "Group 2", null, "fr.insee", List.of(iri2))
        );
        when(ddiRepository.getGroups()).thenReturn(allGroups);
        when(seriesCreatorsPort.getCreatorsForSeries(Set.of(iri1, iri2)))
                .thenReturn(Map.of(iri1, List.of("stamp-A"), iri2, List.of("stamp-B")));

        List<PartialGroup> result = ddiService.getGroupsFilteredByStamp(Set.of("stamp-A", "stamp-B"));

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
