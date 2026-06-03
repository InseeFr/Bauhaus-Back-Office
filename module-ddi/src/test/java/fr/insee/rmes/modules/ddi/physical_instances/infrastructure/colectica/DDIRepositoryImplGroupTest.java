package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi4ToLifecycle33;
import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.UpdateItemStateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DDIRepositoryImplGroupTest {

    @Mock
    private ColecticaClient colecticaClient;

    @Mock
    private ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;

    private final Ddi4ToLifecycle33 ddi4ToLifecycle33 = new Ddi4ToLifecycle33();

    @Mock
    private DDIRepository ddiRepository;

    private static final String BASE_API_URL = "http://localhost:8082/api/v1/";

    @BeforeEach
    void setUp() {
        lenient().when(instanceConfiguration.defaultAgencyId()).thenReturn("fr.insee");
        lenient().when(instanceConfiguration.versionResponsibility()).thenReturn("bauhaus");
        lenient().when(instanceConfiguration.itemFormat()).thenReturn("DC337820-AF3A-4C0B-82F9-CF02535CDE83");
    }

    // --- ColecticaGroupRepository ---

    @Nested
    class GroupRepositoryTests {

        private ColecticaGroupRepository groupRepository;

        @BeforeEach
        void setUp() {
            groupRepository = new ColecticaGroupRepository(
                    colecticaClient, instanceConfiguration, ddi4ToLifecycle33, ddiRepository
            );
        }

        @Test
        void createOrUpdate_shouldTransformAndPostGroup() throws XMLStreamException {
            Ddi4Group group = new Ddi4Group(Ddi4Group.TYPE,
                    CogsDate.ofDateTime("2026-04-02T00:00:00Z"),
                    "urn:ddi:fr.insee:group-uuid:1", "fr.insee", "group-uuid", "1",
                    "bauhaus",
                    new Citation(LangStrings.of("fr-FR", "s1001 Group")),
                    List.of(Reference.of("fr.insee", "su-uuid-1", "1", "StudyUnit")),
                    List.of("http://id.insee.fr/operations/serie/s1001"),
                    "insee:StatisticalOperationSeries"
            );

            groupRepository.createOrUpdate(group);

            ArgumentCaptor<ColecticaCreateItemRequest> captor = ArgumentCaptor.forClass(ColecticaCreateItemRequest.class);
            verify(colecticaClient).createOrUpdateItems(captor.capture());

            ColecticaCreateItemRequest request = captor.getValue();
            assertThat(request).isNotNull();
            assertThat(request.items()).hasSize(1);

            var item = request.items().getFirst();
            assertThat(item.itemType()).isEqualTo("4bd6eef6-99df-40e6-9b11-5b8f64e5cb23");
            assertThat(item.agencyId()).isEqualTo("fr.insee");
            assertThat(item.identifier()).isEqualTo("group-uuid");
            assertThat(item.version()).isEqualTo(1);
            assertThat(item.item())
                    .startsWith("<Fragment")
                    .contains(">urn:ddi:fr.insee:group-uuid:1<")
                    .contains(">s1001 Group<")
                    .contains(">su-uuid-1<");
        }

        @Test
        void deprecateAll_shouldDeprecateAllGroups() {
            when(ddiRepository.getGroups()).thenReturn(List.of(
                    new PartialGroup("group-id-1", "Groupe 1", null, "fr.insee", List.of()),
                    new PartialGroup("group-id-2", "Groupe 2", null, "fr.insee", List.of())
            ));

            groupRepository.deprecateAll();

            ArgumentCaptor<UpdateItemStateRequest> captor = ArgumentCaptor.forClass(UpdateItemStateRequest.class);
            verify(colecticaClient).updateItemState(captor.capture());

            UpdateItemStateRequest body = captor.getValue();
            assertThat(body).isNotNull();
            assertThat(body.state()).isTrue();
            assertThat(body.applyToAllVersions()).isTrue();

            List<UpdateItemStateRequest.ItemIdentifier> ids = body.ids();
            assertThat(ids).hasSize(2);
            assertThat(ids.get(0).identifier()).isEqualTo("group-id-1");
            assertThat(ids.get(1).identifier()).isEqualTo("group-id-2");
        }

        @Test
        void deprecateAll_shouldDoNothingWhenNoGroups() {
            when(ddiRepository.getGroups()).thenReturn(List.of());

            groupRepository.deprecateAll();

            verify(colecticaClient, never()).updateItemState(any());
        }
    }

    // --- ColecticaStudyUnitRepository ---

    @Nested
    class StudyUnitRepositoryTests {

        private ColecticaStudyUnitRepository studyUnitRepository;

        @BeforeEach
        void setUp() {
            studyUnitRepository = new ColecticaStudyUnitRepository(
                    colecticaClient, instanceConfiguration, ddi4ToLifecycle33, null
            );
        }

        @Test
        void createOrUpdate_shouldTransformAndPostStudyUnit() throws XMLStreamException {
            Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(Ddi4StudyUnit.TYPE,
                    CogsDate.ofDateTime("2026-04-02T00:00:00Z"),
                    "urn:ddi:fr.insee:su-uuid:1", "fr.insee", "su-uuid", "1",
                    new Citation(LangStrings.of("fr-FR", "op1 Study Unit")),
                    "http://id.insee.fr/operations/operation/op1",
                    null
            );

            studyUnitRepository.createOrUpdate(studyUnit);

            ArgumentCaptor<ColecticaCreateItemRequest> captor = ArgumentCaptor.forClass(ColecticaCreateItemRequest.class);
            verify(colecticaClient).createOrUpdateItems(captor.capture());

            ColecticaCreateItemRequest request = captor.getValue();
            assertThat(request).isNotNull();
            assertThat(request.items()).hasSize(1);

            var item = request.items().getFirst();
            assertThat(item.itemType()).isEqualTo("30ea0200-7121-4f01-8d21-a931a182b86d");
            assertThat(item.agencyId()).isEqualTo("fr.insee");
            assertThat(item.identifier()).isEqualTo("su-uuid");
            assertThat(item.item())
                    .startsWith("<Fragment")
                    .contains(">urn:ddi:fr.insee:su-uuid:1<")
                    .contains(">op1 Study Unit<")
                    .contains(">http://id.insee.fr/operations/operation/op1<");
        }
    }

    // --- generateDeterministicUuid ---

    @Test
    void generateDeterministicUuid_shouldBeStableForSameUri() {
        String uri = "http://id.insee.fr/operations/serie/s1001";
        assertThat(AbstractColecticaItemRepository.generateDeterministicUuid(uri))
                .isEqualTo(AbstractColecticaItemRepository.generateDeterministicUuid(uri));
    }

    @Test
    void generateDeterministicUuid_shouldDifferForDifferentUris() {
        String uuid1 = AbstractColecticaItemRepository.generateDeterministicUuid("http://id.insee.fr/operations/serie/s1001");
        String uuid2 = AbstractColecticaItemRepository.generateDeterministicUuid("http://id.insee.fr/operations/serie/s1002");
        assertThat(uuid1).isNotEqualTo(uuid2);
    }

    // --- helper ---

    private ColecticaItem createColecticaItem(String identifier, String label) {
        return new ColecticaItem(
                null, Map.of("fr-FR", label), null, null, null,
                0, null, false, null,
                "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23",
                "fr.insee", 1, identifier, null, null,
                "2026-04-02T00:00:00", "bauhaus",
                false, false, false,
                "DC337820-AF3A-4C0B-82F9-CF02535CDE83", 0L, 0
        );
    }
}
