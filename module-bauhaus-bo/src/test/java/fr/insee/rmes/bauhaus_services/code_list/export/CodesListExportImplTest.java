package fr.insee.rmes.bauhaus_services.code_list.export;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodesListExportImplTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    @InjectMocks
    private CodesListExportImpl codesListExport;


    @Test
    void exportCodesList_shouldReturnExportedCodesList() throws Exception {

        CodeListsQueries.setConfig(new ConfigStub());

        JSONObject codeList = new JSONObject().put("labelLg1", "My Code List");
        JSONArray codesArray = new JSONArray()
                .put(new JSONObject().put("code", "001").put("labelLg1", "Code 1"))
                .put(new JSONObject().put("code", "002").put("labelLg1", "Code 2"));

        when(repositoryGestion.getResponseAsObject(anyString()))
                .thenReturn(codeList);

        when(repositoryGestion.getResponseAsArray(anyString()))
                .thenReturn(codesArray);

        String notation = "CL_001";
        ExportedCodesList result = codesListExport.exportCodesList(notation);

        assertNotNull(result);
        assertEquals(notation, result.notation());
        assertEquals("My Code List", result.labelLg1());
        assertNotNull(result.codes());
        assertEquals(2, result.codes().size());
    }
}