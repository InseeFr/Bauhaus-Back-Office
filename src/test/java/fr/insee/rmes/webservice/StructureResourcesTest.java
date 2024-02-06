package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import org.json.JSONArray;
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

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

/**
 * L'utilisateur de "structures et composants" a les droits suivants :
 * • consulter une structure ;
 * • consulter un composant ;
 * • faire une recherche avancée de structure ;
 * • faire une recherche avancée de composant.
 *
 * Le gestionnaire de "structures et composants" est un utilisateur dont le timbre fait partie des timbres figurant dans la propriété dc:contributor de l’objet modifié (liste des timbres des gestionnaires de l’objet). Il a les droits suivants :
 * • les droits de l'utilisateur de "structures et composants"  ;
 * et les droits spécifiques :
 * • créer une structure ;
 * • créer un composant ;
 * • dupliquer une structure ;
 * • modifier une structure ;
 * • modifier un composant ;
 * • supprimer une structure, si elle est provisoire, jamais publiée ;
 * • supprimer un composant, s’il est provisoire, jamais publié ;
 * • publier une structure ;
 * • publier un composant.
 *
 * L'administrateurde "structures et composants" a les droits suivants :
 * • les droits de l'utilisateur de "structures et composants"  ;
 * • les droits du gestionnaire de "structures et composants", quels que soient les timbres figurant dans la propriété dc:contributor de l’objet modifié (liste des timbres des gestionnaires de l’objet) ;
 * et les droits spécifiques :
 * • supprimer une structure, quel que soit son état ;
 * • supprimer un composant, quel que soit son état.
 */

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
    void shouldReturnAccessDeniedExceptionWhenPublishingStructureWithoutAdminRights(@Autowired Config config) {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray(), "fakeStampForDvAndQf");
        Assertions.assertThrows(AccessDeniedException.class, () -> {
            structureResources.publishStructureById("1");
        });
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenPublishingAStructure() throws RmesException {
        authorizeMethodDecider.fakeUser = new User("fakeUser", new JSONArray().put("Administrateur_RMESGNCS"), "fakeStampForDvAndQf");
        when(structureService.publishStructureById(anyString())).thenReturn("result publishing");
        ResponseEntity<?> response = structureResources.publishStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

}
