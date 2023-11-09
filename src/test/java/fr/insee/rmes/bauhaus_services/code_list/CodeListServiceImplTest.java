package fr.insee.rmes.bauhaus_services.code_list;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeListServiceImplTest {

    @Mock
    RepositoryGestion repositoryGestion;

    @InjectMocks
    CodeListServiceImpl codeListService;

    @Test
    void getCodesJson() throws RmesException {
        try (MockedStatic<CodeListQueries> mockedFactory = Mockito.mockStatic(CodeListQueries.class)) {
            mockedFactory.when(() -> CodeListQueries.countCodesForCodeList("notation")).thenReturn("query");
            mockedFactory.when(() -> CodeListQueries.getCodeListItemsByNotation("notation", 1)).thenReturn("query2");

            JSONObject count = new JSONObject();
            count.put("count", 5);
            when(repositoryGestion.getResponseAsObject(eq("query"))).thenReturn(count);

            JSONObject item = new JSONObject();
            item.put("id", "id");
            JSONArray items = new JSONArray();
            items.put(item);
            when(repositoryGestion.getResponseAsArray(eq("query2"))).thenReturn(items);

            assertEquals("{\"total\":5,\"page\":1,\"items\":[{\"id\":\"id\"}]}", codeListService.getCodesJson("notation", 1));
        }
    }

    @Test
    void getCodesForCodeList() throws RmesException {
        try (MockedStatic<CodeListQueries> mockedFactory = Mockito.mockStatic(CodeListQueries.class)) {
            mockedFactory.when(() -> CodeListQueries.countCodesForCodeList("notation")).thenReturn("query");
            mockedFactory.when(() -> CodeListQueries.getDetailedCodes("notation", false, 1)).thenReturn("query2");

            JSONObject count = new JSONObject();
            count.put("count", 5);
            when(repositoryGestion.getResponseAsObject(eq("query"))).thenReturn(count);

            JSONObject item = new JSONObject();
            item.put("id", "id");
            JSONArray items = new JSONArray();
            items.put(item);
            when(repositoryGestion.getResponseAsArray(eq("query2"))).thenReturn(items);

            assertEquals("{\"total\":5,\"page\":1,\"items\":[{\"id\":\"id\"}]}", codeListService.getCodesForCodeList("notation", 1));
        }
    }
}