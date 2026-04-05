package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi3XmlWriter;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaItem;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DDIRepositoryImplGroupTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;

    @Mock
    private ColecticaAuthenticator authenticator;

    @Mock
    private Ddi3XmlWriter ddi3XmlWriter;

    @Mock
    private DDIRepository ddiRepository;

    private static final String TEST_TOKEN = "test-token-123";
    private static final String BASE_API_URL = "http://localhost:8082/api/v1/";

    @BeforeEach
    void setUp() {
        lenient().when(authenticator.executeWithAuth(any())).thenAnswer(invocation -> {
            java.util.function.Function<String, ?> function = invocation.getArgument(0);
            return function.apply(TEST_TOKEN);
        });

        lenient().when(instanceConfiguration.baseApiUrl()).thenReturn(BASE_API_URL);
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
                    restTemplate, instanceConfiguration, authenticator, ddi3XmlWriter, ddiRepository
            );
        }

        @Test
        void createOrUpdate_shouldTransformAndPostGroup() throws XMLStreamException {
            Ddi4Group group = new Ddi4Group(
                    "true", "2026-04-02T00:00:00Z",
                    "urn:ddi:fr.insee:group-uuid:1", "fr.insee", "group-uuid", "1",
                    "bauhaus",
                    new Citation(new Title(new StringValue("fr-FR", "s1001 Group"))),
                    List.of(new StudyUnitReference("fr.insee", "su-uuid-1", "1", "StudyUnit")),
                    "http://id.insee.fr/operations/serie/s1001",
                    "insee:StatisticalOperationSeries"
            );

            when(ddi3XmlWriter.buildGroupXml(group)).thenReturn("<Fragment>group-xml</Fragment>");

            groupRepository.createOrUpdate(group);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<HttpEntity<ColecticaCreateItemRequest>> captor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).postForObject(eq(BASE_API_URL + "item"), captor.capture(), eq(String.class));

            ColecticaCreateItemRequest request = captor.getValue().getBody();
            assertThat(request).isNotNull();
            assertThat(request.items()).hasSize(1);

            var item = request.items().getFirst();
            assertThat(item.itemType()).isEqualTo("4bd6eef6-99df-40e6-9b11-5b8f64e5cb23");
            assertThat(item.agencyId()).isEqualTo("fr.insee");
            assertThat(item.identifier()).isEqualTo("group-uuid");
            assertThat(item.version()).isEqualTo(1);
            assertThat(item.item()).isEqualTo("<Fragment>group-xml</Fragment>");
        }

        @Test
        @SuppressWarnings("unchecked")
        void deprecateAll_shouldDeprecateAllGroups() {
            ColecticaItem item1 = createColecticaItem("group-id-1", "Groupe 1");
            ColecticaItem item2 = createColecticaItem("group-id-2", "Groupe 2");

            when(ddiRepository.getGroups()).thenReturn(List.of(
                    new PartialGroup("group-id-1", "Groupe 1", null, "fr.insee"),
                    new PartialGroup("group-id-2", "Groupe 2", null, "fr.insee")
            ));

            groupRepository.deprecateAll();

            ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).postForObject(eq(BASE_API_URL + "item/_updateState"), captor.capture(), eq(String.class));

            Map<String, Object> body = captor.getValue().getBody();
            assertThat(body).isNotNull();
            assertThat(body.get("state")).isEqualTo(true);
            assertThat(body.get("applyToAllVersions")).isEqualTo(true);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ids = (List<Map<String, Object>>) body.get("ids");
            assertThat(ids).hasSize(2);
            assertThat(ids.get(0).get("identifier")).isEqualTo("group-id-1");
            assertThat(ids.get(1).get("identifier")).isEqualTo("group-id-2");
        }

        @Test
        void deprecateAll_shouldDoNothingWhenNoGroups() {
            when(ddiRepository.getGroups()).thenReturn(List.of());

            groupRepository.deprecateAll();

            verify(restTemplate, never()).postForObject(eq(BASE_API_URL + "item/_updateState"), any(), any());
        }
    }

    // --- ColecticaStudyUnitRepository ---

    @Nested
    class StudyUnitRepositoryTests {

        private ColecticaStudyUnitRepository studyUnitRepository;

        @BeforeEach
        void setUp() {
            studyUnitRepository = new ColecticaStudyUnitRepository(
                    restTemplate, instanceConfiguration, authenticator, ddi3XmlWriter, null
            );
        }

        @Test
        void createOrUpdate_shouldTransformAndPostStudyUnit() throws XMLStreamException {
            Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(
                    "true", "2026-04-02T00:00:00Z",
                    "urn:ddi:fr.insee:su-uuid:1", "fr.insee", "su-uuid", "1",
                    new Citation(new Title(new StringValue("fr-FR", "op1 Study Unit"))),
                    "http://id.insee.fr/operations/operation/op1",
                    null
            );

            when(ddi3XmlWriter.buildStudyUnitXml(studyUnit)).thenReturn("<Fragment>studyunit-xml</Fragment>");

            studyUnitRepository.createOrUpdate(studyUnit);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<HttpEntity<ColecticaCreateItemRequest>> captor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).postForObject(eq(BASE_API_URL + "item"), captor.capture(), eq(String.class));

            ColecticaCreateItemRequest request = captor.getValue().getBody();
            assertThat(request).isNotNull();
            assertThat(request.items()).hasSize(1);

            var item = request.items().getFirst();
            assertThat(item.itemType()).isEqualTo("30ea0200-7121-4f01-8d21-a931a182b86d");
            assertThat(item.agencyId()).isEqualTo("fr.insee");
            assertThat(item.identifier()).isEqualTo("su-uuid");
            assertThat(item.item()).isEqualTo("<Fragment>studyunit-xml</Fragment>");
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
