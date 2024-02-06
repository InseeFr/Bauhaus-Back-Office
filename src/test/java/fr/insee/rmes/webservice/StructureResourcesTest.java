package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.*;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
class StructureResourcesTest {
    @MockBean
    StructureService structureService;

    @MockBean
    StructureComponent structureComponentService;

    @Autowired
    AuthorizeMethodDecider authorizeMethodDecider;

    @Autowired
    StructureResources structureResources;

    @MockBean
    RepositoryGestion repositoryGestion;

    @Test
    @WithMockUser
    void shouldReturn200WhenFetchingStructures() throws RmesException {
        when(structureService.getStructures()).thenReturn("result");
        ResponseEntity<?> response = structureResources.getStructures();
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenFetchingStructuresForSearch() throws RmesException {
        when(structureService.getStructuresForSearch()).thenReturn("result");
        ResponseEntity<?> response = structureResources.getStructuresForSearch();
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenFetchingComponents() throws RmesException {
        when(structureComponentService.getComponents()).thenReturn("result");
        ResponseEntity<?> response = structureResources.getComponents();
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenFetchingComponentById() throws RmesException {
        when(structureComponentService.getComponent(anyString())).thenReturn("result");
        ResponseEntity<?> response = structureResources.getComponentById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenFetchingComponentsForSearch() throws RmesException {
        when(structureComponentService.getComponentsForSearch()).thenReturn("result");
        ResponseEntity<?> response = structureResources.getComponentsForSearch();
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenFetchingStructureById() throws RmesException {
        when(structureService.getStructureById(anyString())).thenReturn("result");
        ResponseEntity<?> response = structureResources.getStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }


    @Test
    @WithMockUser
    void shouldReturn200WhenPublishingAStructure() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put("Administrateur_RMESGNCS"), "fakeStampForDvAndQf");
        when(structureService.publishStructureById(anyString())).thenReturn("result publishing");
        ResponseEntity<?> response = structureResources.publishStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenDeletingAStructure() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put("Administrateur_RMESGNCS"), "fakeStampForDvAndQf");
        doNothing().when(structureService).deleteStructure(anyString());
        ResponseEntity<?> response = structureResources.deleteStructure("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenDeletingAComponent() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put("Administrateur_RMESGNCS"), "fakeStampForDvAndQf");
        doNothing().when(structureComponentService).deleteComponent(anyString());
        ResponseEntity<?> response = structureResources.deleteComponent("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenCreatingAComponentWhenAdministrator() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put("Administrateur_RMESGNCS"), "fakeStampForDvAndQf");
        when(structureComponentService.createComponent(anyString())).thenReturn("");
        ResponseEntity<?> response = structureResources.createComponent("");
        Assertions.assertEquals(201, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenCreatingAComponentWhenContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureComponentService.createComponent(anyString())).thenReturn("");

        JSONObject component = new JSONObject();
        component.put("contributor", "fakeStampForDvAndQf");
        ResponseEntity<?> response = structureResources.createComponent(component.toString());
        Assertions.assertEquals(201, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldThrowAnExceptionWhenCreatingAComponentWhenNotContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureComponentService.createComponent(anyString())).thenReturn("");

        JSONObject component = new JSONObject();
        component.put("contributor", "stamp");
        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.createComponent(component.toString());
        });
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenCreatingAStructureWhenAdministrator() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put("Administrateur_RMESGNCS"), "fakeStampForDvAndQf");
        when(structureService.setStructure(anyString())).thenReturn("");
        ResponseEntity<?> response = structureResources.createStructure("");
        Assertions.assertEquals(201, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenCreatingAStructureWhenContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureService.setStructure(anyString())).thenReturn("");

        JSONObject structure = new JSONObject();
        structure.put("contributor", "fakeStampForDvAndQf");
        ResponseEntity<?> response = structureResources.createStructure(structure.toString());
        Assertions.assertEquals(201, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldThrowAnExceptionWhenCreatingAStructureWhenNotContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureService.setStructure(anyString())).thenReturn("");

        JSONObject structure = new JSONObject();
        structure.put("contributor", "stamp");
        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.createStructure(structure.toString());
        });
    }

    //////////////

    @Test
    @WithMockUser
    void shouldReturn200WhenUpdatingAStructureWhenAdministrator() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put("Administrateur_RMESGNCS"), "fakeStampForDvAndQf");
        when(structureService.setStructure(anyString(), anyString())).thenReturn("");
        ResponseEntity<?> response = structureResources.setStructure("1", new JSONObject().toString());
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenUpdatingAStructureWhenContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureService.setStructure(anyString(), anyString())).thenReturn("");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "fakeStampForDvAndQf"));


        ResponseEntity<?> response = structureResources.setStructure("1", "");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldThrowAnExceptionWhenUpdatingAStructureWhenNotContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureService.setStructure(anyString())).thenReturn("");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "stamp"));

        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.setStructure("1", new JSONObject().toString());
        });
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenUpdatingAComponentWhenAdministrator() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put("Administrateur_RMESGNCS"), "fakeStampForDvAndQf");
        when(structureComponentService.updateComponent(anyString(), any())).thenReturn("");
        ResponseEntity<?> response = structureResources.updateComponentById("1", new JSONObject().toString());
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenUpdatingAComponentWhenContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureComponentService.updateComponent(anyString(), any())).thenReturn("");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "fakeStampForDvAndQf"));


        ResponseEntity<?> response = structureResources.updateComponentById("1", "");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldThrowAnExceptionWhenUpdatingAComponentWhenNotContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureComponentService.updateComponent(anyString(), any())).thenReturn("");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "stamp"));

        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.updateComponentById("1", new JSONObject().toString());
        });
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenDeletingAStructureWhenContributorAndUnpublished() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "fakeStampForDvAndQf").put("validationState", ValidationStatus.UNPUBLISHED.toString()));
        doNothing().when(structureService).deleteStructure(anyString());
        ResponseEntity<?> response = structureResources.deleteStructure("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldThrowAnErrorWhenDeletingAStructureWhenUnpublishedBuNotContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "stamp").put("validationState", ValidationStatus.UNPUBLISHED.toString()));
        doNothing().when(structureService).deleteStructure(anyString());
        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.deleteStructure("1");
        });
    }

    @Test
    @WithMockUser
    void shouldThrowAnErrorWhenDeletingAStructureWhenPublishedBuContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "fakeStampForDvAndQf").put("validationState", ValidationStatus.VALIDATED.toString()));
        doNothing().when(structureService).deleteStructure(anyString());
        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.deleteStructure("1");
        });
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenDeletingAStructureWhenAdministrator() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "fakeStampForDvAndQf").put("validationState", ValidationStatus.UNPUBLISHED.toString()));
        doNothing().when(structureService).deleteStructure(anyString());
        ResponseEntity<?> response = structureResources.deleteStructure("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenDeletingAComponentWhenContributorAndUnpublished() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "fakeStampForDvAndQf").put("validationState", ValidationStatus.UNPUBLISHED.toString()));
        doNothing().when(structureComponentService).deleteComponent(anyString());
        ResponseEntity<?> response = structureResources.deleteComponent("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldThrowAnErrorWhenDeletingAComponentWhenUnpublishedBuNotContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "stamp").put("validationState", ValidationStatus.UNPUBLISHED.toString()));
        doNothing().when(structureComponentService).deleteComponent(anyString());
        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.deleteComponent("1");
        });
    }

    @Test
    @WithMockUser
    void shouldThrowAnErrorWhenDeletingAComponentWhenPublishedBuContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "fakeStampForDvAndQf").put("validationState", ValidationStatus.VALIDATED.toString()));
        doNothing().when(structureComponentService).deleteComponent(anyString());
        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.deleteComponent("1");
        });
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenDeletingAComponentWhenAdministrator() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "fakeStampForDvAndQf").put("validationState", ValidationStatus.UNPUBLISHED.toString()));
        doNothing().when(structureComponentService).deleteComponent(anyString());
        ResponseEntity<?> response = structureResources.deleteComponent("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenPublishingAComponentWhenAdministrator() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put("Administrateur_RMESGNCS"), "fakeStampForDvAndQf");
        when(structureComponentService.publishComponent(anyString())).thenReturn("");
        ResponseEntity<?> response = structureResources.publishComponentById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenPublishingAComponentWhenContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureComponentService.publishComponent(anyString())).thenReturn("");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "fakeStampForDvAndQf"));


        ResponseEntity<?> response = structureResources.publishComponentById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldThrowAnExceptionWhenPublishingAComponentWhenNotContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureComponentService.publishComponent(anyString())).thenReturn("");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "stamp"));

        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.publishComponentById("1");
        });
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenPublishingAStructureWhenAdministrator() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put("Administrateur_RMESGNCS"), "fakeStampForDvAndQf");
        when(structureService.publishStructureById(anyString())).thenReturn("");
        ResponseEntity<?> response = structureResources.publishStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenPublishingAStructureWhenContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureService.publishStructureById(anyString())).thenReturn("");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "fakeStampForDvAndQf"));


        ResponseEntity<?> response = structureResources.publishStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @WithMockUser
    void shouldThrowAnExceptionWhenPublishingAStructureWhenNotContributor() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put(""), "fakeStampForDvAndQf");
        when(structureService.publishStructureById(anyString())).thenReturn("");
        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject().put("contributor", "stamp"));

        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.publishStructureById("1");
        });
    }
}
