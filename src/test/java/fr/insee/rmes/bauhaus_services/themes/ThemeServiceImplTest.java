package fr.insee.rmes.bauhaus_services.themes;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "fr.insee.rmes.bauhaus.theme.graph=graph",
        "fr.insee.rmes.bauhaus.theme.conceptSchemeFilter=filter1"
})
class ThemeServiceImplTest {
    @MockitoBean
    RepositoryGestion repositoryGestion;

    @Autowired
    ThemeServiceImpl themeService;

    @Test
    void shouldReturnDatasets() throws RmesException {
        JSONArray array = new JSONArray();
        array.put("result");

        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        try (MockedStatic<ThemeQueries> mockedFactory = Mockito.mockStatic(ThemeQueries.class)) {
            mockedFactory.when(() -> ThemeQueries.getThemes(eq("filter2"), eq("graph"), eq("filter1"))).thenReturn("query");
            JSONArray response = themeService.getThemes("filter2");
            Assertions.assertEquals("result", response.get(0));
        }
    }
}