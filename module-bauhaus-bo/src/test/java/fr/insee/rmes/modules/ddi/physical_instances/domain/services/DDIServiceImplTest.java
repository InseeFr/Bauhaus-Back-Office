package fr.insee.rmes.modules.ddi.physical_instances.domain.services;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StringValue;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StudyUnitReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Title;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.TopLevelReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DDIServiceImplTest {

    @Mock
    private DDIRepository ddiRepository;

    private DDIServiceImpl ddiService;

    @BeforeEach
    void setUp() {
        ddiService = new DDIServiceImpl(ddiRepository);
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
    void shouldUpdatePhysicalInstance() {
        // Given
        String agencyId = "fr.insee";
        String instanceId = "test-id";
        UpdatePhysicalInstanceRequest request = new UpdatePhysicalInstanceRequest(
            "Updated Physical Instance Label",
            "Updated DataRelationship Name"
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
            "New DataRelationship Name"
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
                new PartialGroup("group-1", "Base permanente des équipements", new Date(), "fr.insee"),
                new PartialGroup("group-2", "Recensement de la population", new Date(), "fr.insee")
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

        Citation citation = new Citation(new Title(new StringValue("fr-FR", "Base permanente des équipements")));
        StudyUnitReference suRef1 = new StudyUnitReference(agencyId, "su-1", "1", "StudyUnit");
        StudyUnitReference suRef2 = new StudyUnitReference(agencyId, "su-2", "1", "StudyUnit");

        Ddi4Group group = new Ddi4Group(
            "true", "2025-01-09T09:00:00Z",
            "urn:ddi:fr.insee:" + groupId + ":1",
            agencyId, groupId, "1",
            "bauhaus", citation, List.of(suRef1, suRef2)
        );

        Ddi4StudyUnit studyUnit1 = new Ddi4StudyUnit(
            "true", "2025-01-09T09:00:00Z",
            "urn:ddi:fr.insee:su-1:1",
            agencyId, "su-1", "1",
            new Citation(new Title(new StringValue("fr-FR", "BPE 2021")))
        );

        Ddi4StudyUnit studyUnit2 = new Ddi4StudyUnit(
            "true", "2025-01-09T09:00:00Z",
            "urn:ddi:fr.insee:su-2:1",
            agencyId, "su-2", "1",
            new Citation(new Title(new StringValue("fr-FR", "BPE 2022")))
        );

        TopLevelReference topLevelRef = new TopLevelReference(agencyId, groupId, "1", "Group");

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
        assertEquals("Base permanente des équipements", result.group().get(0).citation().title().string().text());
        assertEquals(2, result.studyUnit().size());
        assertEquals("BPE 2021", result.studyUnit().get(0).citation().title().string().text());
        assertEquals("BPE 2022", result.studyUnit().get(1).citation().title().string().text());

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
}
