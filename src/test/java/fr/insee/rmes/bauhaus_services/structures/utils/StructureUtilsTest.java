package fr.insee.rmes.bauhaus_services.structures.utils;


import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.structures.Structure;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
class StructureUtilsTest {
    @InjectMocks
    StructureUtils structureUtils = new StructureUtils();
    @Mock
    StructureUtils mockStructureUtils;
    @MockBean
    RepositoryGestion repositoryGestion;
    @Autowired
    Config config;


    public static final String VALIDATION_STATUS = "{\"state\":\"Published\"}";


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

}
