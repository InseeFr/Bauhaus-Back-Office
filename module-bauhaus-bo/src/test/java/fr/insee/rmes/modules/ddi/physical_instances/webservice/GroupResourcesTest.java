package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialGroupResponse;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupResourcesTest {

    @Mock
    private GroupService groupService;

    @Mock
    private DDIService ddiService;

    @Mock
    private UserProvider userProvider;

    @Mock
    private RbacFetcher rbacFetcher;

    @InjectMocks
    private GroupResources groupResources;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("");
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // --- plain /ddi/groups list + create ---

    @Test
    void getGroups_shouldReturn200WithList() {
        List<PartialGroup> groups = List.of(
                new PartialGroup("group-1", "Group 1", new Date(), "fr.insee", List.of()),
                new PartialGroup("group-2", "Group 2", new Date(), "fr.insee", List.of())
        );
        when(groupService.getAll()).thenReturn(groups);

        ResponseEntity<List<PartialGroup>> response = groupResources.getGroups();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo("group-1");
        verify(groupService).getAll();
    }

    @Test
    void getGroups_shouldReturn500OnError() {
        when(groupService.getAll()).thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<PartialGroup>> response = groupResources.getGroups();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void createOrUpdateGroup_shouldReturn201() {
        Ddi4Group group = new Ddi4Group(Ddi4Group.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus-test",
                new Citation(LangStrings.of("fr-FR", "Test Group")),
                List.of(Reference.of("fr.insee", "su-id", "1", "StudyUnit")),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries"
        );

        ResponseEntity<Void> response = groupResources.createOrUpdateGroup(group);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(groupService).createOrUpdate(group);
    }

    @Test
    void createOrUpdateGroup_shouldReturn500OnError() {
        Ddi4Group group = new Ddi4Group(Ddi4Group.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus-test",
                new Citation(LangStrings.of("fr-FR", "Test Group")),
                List.of(),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries"
        );

        doThrow(new RuntimeException("Colectica error")).when(groupService).createOrUpdate(group);

        ResponseEntity<Void> response = groupResources.createOrUpdateGroup(group);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- HATEOAS /ddi/group browse (stamp-aware) + /ddi/group/{agencyId}/{id} detail ---

    @Test
    void getGroupResponses_shouldReturn200WithLinks() throws Exception, MissingUserInformationException {
        List<PartialGroup> expectedGroups = new ArrayList<>();
        expectedGroups.add(new PartialGroup("group-1", "Base permanente des équipements", new Date(), "fr.insee", List.of()));
        expectedGroups.add(new PartialGroup("group-2", "Recensement de la population", new Date(), "fr.insee", List.of()));
        when(userProvider.findUser()).thenReturn(Optional.empty());
        when(rbacFetcher.getApplicationActionStrategyByRole(any(), eq(RBAC.Module.DDI_PHYSICALINSTANCE), eq(RBAC.Privilege.READ)))
                .thenReturn(RBAC.Strategy.ALL);
        when(ddiService.getGroups()).thenReturn(expectedGroups);

        ResponseEntity<List<PartialGroupResponse>> response = groupResources.getGroupResponses();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        List<PartialGroupResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("group-1", result.getFirst().getId());
        assertEquals("Base permanente des équipements", result.getFirst().getLabel());
        assertEquals(1, result.getFirst().getLinks().toList().size());
        assertEquals("http://localhost:8080/ddi/group/fr.insee/group-1", result.getFirst().getRequiredLink("self").getHref());

        assertEquals("group-2", result.get(1).getId());
        assertEquals("http://localhost:8080/ddi/group/fr.insee/group-2", result.get(1).getRequiredLink("self").getHref());

        verify(ddiService).getGroups();
    }

    @Test
    void getGroupResponses_shouldFilterByStampWhenStrategyIsStamp() throws Exception, MissingUserInformationException {
        String iri = "http://id.insee.fr/operations/serie/s1001";
        List<PartialGroup> filteredGroups = List.of(
                new PartialGroup("group-1", "Base permanente des équipements", new Date(), "fr.insee", List.of(iri))
        );
        User stampUser = new User("user-1", List.of("role-stamp"), Set.of("stamp-A"));
        when(userProvider.findUser()).thenReturn(Optional.of(stampUser));
        when(rbacFetcher.getApplicationActionStrategyByRole(any(), eq(RBAC.Module.DDI_PHYSICALINSTANCE), eq(RBAC.Privilege.READ)))
                .thenReturn(RBAC.Strategy.STAMP);
        when(ddiService.getGroupsFilteredByStamp(Set.of("stamp-A"))).thenReturn(filteredGroups);

        ResponseEntity<List<PartialGroupResponse>> response = groupResources.getGroupResponses();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        List<PartialGroupResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("group-1", result.getFirst().getId());

        verify(ddiService).getGroupsFilteredByStamp(Set.of("stamp-A"));
    }

    @Test
    void getDdi4Group_shouldReturnGroupDetail() {
        String agencyId = "fr.insee";
        String id = "10a689ce-7006-429b-8e84-036b7787b422";
        Ddi4GroupResponse expectedResponse = createMockDdi4GroupResponse();
        when(ddiService.getDdi4Group(agencyId, id)).thenReturn(expectedResponse);

        ResponseEntity<Ddi4GroupResponse> result = groupResources.getDdi4Group(agencyId, id);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());

        Ddi4GroupResponse responseBody = result.getBody();
        assertNotNull(responseBody);
        assertEquals("test-schema", responseBody.schema());
        assertEquals(1, responseBody.group().size());
        assertEquals(2, responseBody.studyUnit().size());
        assertEquals("10a689ce-7006-429b-8e84-036b7787b422", responseBody.group().get(0).id());

        verify(ddiService).getDdi4Group(agencyId, id);
    }

    // --- /ddi/groups/{agencyId}/{id}/logical-products + nested code-list-scheme ---

    @Test
    void getGroupLogicalProducts_shouldReturn200WithList() {
        List<PartialLogicalProduct> logicalProducts = List.of(
                new PartialLogicalProduct("lp-1", "Produit Logique 1", new Date(), "fr.insee"),
                new PartialLogicalProduct("lp-2", "Produit Logique 2", new Date(), "fr.insee")
        );
        when(ddiService.getLogicalProductsByGroup("fr.insee", "group-1")).thenReturn(logicalProducts);

        ResponseEntity<List<PartialLogicalProduct>> response =
                groupResources.getGroupLogicalProducts("fr.insee", "group-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo("lp-1");
        assertThat(response.getBody().get(1).id()).isEqualTo("lp-2");
        verify(ddiService).getLogicalProductsByGroup("fr.insee", "group-1");
    }

    @Test
    void getGroupLogicalProducts_shouldReturn500OnError() {
        when(ddiService.getLogicalProductsByGroup("fr.insee", "group-1"))
                .thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<PartialLogicalProduct>> response =
                groupResources.getGroupLogicalProducts("fr.insee", "group-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getLogicalProductCodeListSchemes_shouldReturn200WithList() {
        List<PartialCodeListScheme> schemes = List.of(
                new PartialCodeListScheme("cls-1", "Schéma 1", new Date(), "fr.insee"),
                new PartialCodeListScheme("cls-2", "Schéma 2", new Date(), "fr.insee")
        );
        when(ddiService.getCodeListSchemesByLogicalProduct("fr.insee", "lp-1")).thenReturn(schemes);

        ResponseEntity<List<PartialCodeListScheme>> response =
                groupResources.getLogicalProductCodeListSchemes("fr.insee", "group-1", "fr.insee", "lp-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo("cls-1");
        assertThat(response.getBody().get(1).id()).isEqualTo("cls-2");
        verify(ddiService).getCodeListSchemesByLogicalProduct("fr.insee", "lp-1");
    }

    @Test
    void getLogicalProductCodeListSchemes_shouldReturn500OnError() {
        when(ddiService.getCodeListSchemesByLogicalProduct("fr.insee", "lp-1"))
                .thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<PartialCodeListScheme>> response =
                groupResources.getLogicalProductCodeListSchemes("fr.insee", "group-1", "fr.insee", "lp-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getCodeListSchemeCodesLists_shouldReturn200WithList() {
        List<PartialCodesList> codeLists = List.of(
                new PartialCodesList("code-list-1", "Liste 1", new Date(), "fr.insee"),
                new PartialCodesList("code-list-2", "Liste 2", new Date(), "fr.insee")
        );
        when(ddiService.getCodeListsByCodeListScheme("fr.insee", "cls-1")).thenReturn(codeLists);

        ResponseEntity<List<PartialCodesList>> response = groupResources.getCodeListSchemeCodesLists(
                "fr.insee", "group-1", "fr.insee", "lp-1", "fr.insee", "cls-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo("code-list-1");
        assertThat(response.getBody().get(1).id()).isEqualTo("code-list-2");
        verify(ddiService).getCodeListsByCodeListScheme("fr.insee", "cls-1");
    }

    @Test
    void getCodeListSchemeCodesLists_shouldReturn500OnError() {
        when(ddiService.getCodeListsByCodeListScheme("fr.insee", "cls-1"))
                .thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<PartialCodesList>> response = groupResources.getCodeListSchemeCodesLists(
                "fr.insee", "group-1", "fr.insee", "lp-1", "fr.insee", "cls-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- /ddi/groups/{agencyId}/{id}/codes-list (agrégation tous LP/CLS du group) ---

    @Test
    void getGroupCodesLists_shouldReturn200WithList() {
        List<PartialCodesList> codeLists = List.of(
                new PartialCodesList("cl-1", "Liste 1", new Date(), "fr.insee"),
                new PartialCodesList("cl-2", "Liste 2", new Date(), "fr.insee")
        );
        when(ddiService.getCodeListsByGroup("fr.insee", "group-1")).thenReturn(codeLists);

        ResponseEntity<List<PartialCodesList>> response =
                groupResources.getGroupCodesLists("fr.insee", "group-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo("cl-1");
        assertThat(response.getBody().get(1).id()).isEqualTo("cl-2");
        verify(ddiService).getCodeListsByGroup("fr.insee", "group-1");
    }

    @Test
    void getGroupCodesLists_shouldReturn500OnError() {
        when(ddiService.getCodeListsByGroup("fr.insee", "group-1"))
                .thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<PartialCodesList>> response =
                groupResources.getGroupCodesLists("fr.insee", "group-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- /ddi/groups/{agencyId}/{id}/missing-codes-list (valeurs sentinelles, cf. #1566) ---

    @Test
    void getGroupMissingCodesLists_shouldReturn200WithList() {
        List<PartialCodesList> codeLists = List.of(
                new PartialCodesList("cl-1", "Sentinelles âge", new Date(), "fr.insee"));
        when(ddiService.getMissingCodesListsByGroup("fr.insee", "group-1")).thenReturn(codeLists);

        ResponseEntity<List<PartialCodesList>> response =
                groupResources.getGroupMissingCodesLists("fr.insee", "group-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).id()).isEqualTo("cl-1");
        verify(ddiService).getMissingCodesListsByGroup("fr.insee", "group-1");
    }

    @Test
    void getGroupMissingCodesLists_shouldReturn500OnError() {
        when(ddiService.getMissingCodesListsByGroup("fr.insee", "group-1"))
                .thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<PartialCodesList>> response =
                groupResources.getGroupMissingCodesLists("fr.insee", "group-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- /ddi/groups/{agencyId}/{id}/missing-values-representations (valeurs sentinelles, cf. #1566) ---

    @Test
    void getGroupMissingValuesRepresentations_shouldReturn200WithList() {
        List<PartialMissingValuesRepresentation> representations = List.of(
                new PartialMissingValuesRepresentation("mmvr-1", "fr.insee", "1",
                        "Valeurs sentinelles NSP/REF", "cl-sentinelles", List.of("NSP", "REF")));
        when(ddiService.getMissingValuesRepresentationsByGroup("fr.insee", "group-1"))
                .thenReturn(representations);

        ResponseEntity<List<PartialMissingValuesRepresentation>> response =
                groupResources.getGroupMissingValuesRepresentations("fr.insee", "group-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).id()).isEqualTo("mmvr-1");
        assertThat(response.getBody().get(0).codeValues()).containsExactly("NSP", "REF");
        verify(ddiService).getMissingValuesRepresentationsByGroup("fr.insee", "group-1");
    }

    @Test
    void getGroupMissingValuesRepresentations_shouldReturn500OnError() {
        when(ddiService.getMissingValuesRepresentationsByGroup("fr.insee", "group-1"))
                .thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<PartialMissingValuesRepresentation>> response =
                groupResources.getGroupMissingValuesRepresentations("fr.insee", "group-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Ddi4GroupResponse createMockDdi4GroupResponse() {
        Citation citation = new Citation(LangStrings.of("fr-FR", "Base permanente des équipements"));

        Reference suRef1 = Reference.of("fr.insee", "89f5e04d-da22-485f-9c08-5fbe452b6c90", "1", "StudyUnit");
        Reference suRef2 = Reference.of("fr.insee", "820a7c14-0ac4-42bc-a8c1-d39f60e304ee", "1", "StudyUnit");

        Ddi4Group group = new Ddi4Group(Ddi4Group.TYPE,
            CogsDate.ofDateTime("2025-01-09T09:00:00.000000Z"),
            "urn:ddi:fr.insee:10a689ce-7006-429b-8e84-036b7787b422:1",
            "fr.insee", "10a689ce-7006-429b-8e84-036b7787b422", "1",
            "abcde", citation, List.of(suRef1, suRef2),
            List.of("http://id.insee.fr/operations/serie/s1001"),
            "insee:StatisticalOperationSeries"
        );

        Ddi4StudyUnit studyUnit1 = new Ddi4StudyUnit(Ddi4StudyUnit.TYPE,
            CogsDate.ofDateTime("2025-01-09T09:00:00.000000Z"),
            "urn:ddi:fr.insee:89f5e04d-da22-485f-9c08-5fbe452b6c90:1",
            "fr.insee", "89f5e04d-da22-485f-9c08-5fbe452b6c90", "1",
            new Citation(LangStrings.of("fr-FR", "BPE 2021")),
            "http://id.insee.fr/operations/operation/op1",
            null
        );

        Ddi4StudyUnit studyUnit2 = new Ddi4StudyUnit(Ddi4StudyUnit.TYPE,
            CogsDate.ofDateTime("2025-01-09T09:00:00.000000Z"),
            "urn:ddi:fr.insee:820a7c14-0ac4-42bc-a8c1-d39f60e304ee:1",
            "fr.insee", "820a7c14-0ac4-42bc-a8c1-d39f60e304ee", "1",
            new Citation(LangStrings.of("fr-FR", "BPE 2022")),
            "http://id.insee.fr/operations/operation/op2",
            null
        );

        Reference topLevelRef = Reference.of(
            "fr.insee", "10a689ce-7006-429b-8e84-036b7787b422", "1", "Group"
        );

        return new Ddi4GroupResponse(
            "test-schema",
            List.of(topLevelRef),
            List.of(group),
            List.of(studyUnit1, studyUnit2)
        );
    }
}
