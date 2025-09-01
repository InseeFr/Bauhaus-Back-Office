package fr.insee.rmes.bauhaus_services.structures.utils;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesBadRequestException;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/bauhaus_services/structures/utils/StructureUtilsTest.java
import fr.insee.rmes.onion.domain.exceptions.RmesException;
=======
>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/test/java/fr/insee/rmes/bauhaus_services/structures/utils/StructureUtilsTest.java
import fr.insee.rmes.model.structures.Structure;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class StructureUtilsTest {
    @InjectMocks
    StructureUtils structureUtils = new StructureUtils();

    @MockitoBean
    RepositoryGestion repositoryGestion;

    @Autowired
    Config config;

    public static final String VALIDATION_STATUS = "{\"state\":\"Published\"}";
    public String fakeJsonObjectBody = "This a fake body of JsonObject";

    @Test
    void shouldReturnBadRequestExceptionIfPublishedStructure() throws RmesException {
        JSONObject mockJSON = new JSONObject(VALIDATION_STATUS);
        StructureQueries.setConfig(config);
        Structure structure = new Structure();
        structure.setId("id");
        when(repositoryGestion.getResponseAsObject(Mockito.anyString())).thenReturn(mockJSON);
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureUtils.deleteStructure("id"));
        Assertions.assertEquals("{\"code\":1103,\"message\":\"Only unpublished codelist can be deleted\"}", exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenSetStructure()  {
        RmesException exception = assertThrows(RmesException.class, () -> structureUtils.setStructure(fakeJsonObjectBody));
        Assertions.assertTrue( exception.getDetails().contains("{\"details\":\"IOException\",\"message\":\"Unrecognized token"));
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenCreatorEmpty()  {
        JSONObject jsonObject = new JSONObject().put(Constants.CREATOR,"");
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureUtils.publishStructure(jsonObject));
        Assertions.assertEquals(("{\"code\":1004,\"details\":\"[]\",\"message\":\"The creator should not be empty\"}"), exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenCreatorNull()  {
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureUtils.publishStructure(new JSONObject()));
        Assertions.assertEquals(("{\"code\":1004,\"details\":\"[]\",\"message\":\"The creator should not be empty\"}"), exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenDisseminationStatusNull()  {
        JSONObject jsonObject = new JSONObject().put(Constants.CREATOR,"creatorExample");
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureUtils.publishStructure(jsonObject));
        Assertions.assertEquals("{\"code\":1005,\"details\":\"[]\",\"message\":\"The dissemination status should not be empty\"}",exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenDisseminationStatusIsEmpty()  {
        JSONObject jsonObject = new JSONObject().put(Constants.CREATOR,"creatorExample").put("disseminationStatus","");
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureUtils.publishStructure(jsonObject));
        Assertions.assertEquals("{\"code\":1005,\"details\":\"[]\",\"message\":\"The dissemination status should not be empty\"}",exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenSetStructureWithIdAndBody()  {
       RmesException exception = assertThrows(RmesException.class, () -> structureUtils.setStructure("idExample",fakeJsonObjectBody));
       Assertions.assertTrue(exception.getDetails().contains("{\"details\":\"IOException\""));
    }

}
