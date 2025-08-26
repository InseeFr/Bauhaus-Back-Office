package fr.insee.rmes.bauhaus_services.themes;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceImplTest {

    @Test
    void getThemes_returnsJSONArray_fromRepository() throws Exception {
        RepositoryGestion repositoryGestion = mock(RepositoryGestion.class);
        String themesConceptSchemeFilter = "filter1";
        String datasetsThemeGraph = "http://example.org/graph/themes";
        String schemeFilter = "http://ex/s1";

        ThemeServiceImpl service = new ThemeServiceImpl(datasetsThemeGraph, themesConceptSchemeFilter, repositoryGestion);

        String expectedSparql = "SELECT * WHERE { ?s ?p ?o }";
        JSONArray expected = new JSONArray("[{\"id\":\"t1\"},{\"id\":\"t2\"}]");

        try (MockedStatic<ThemeQueries> mocked = mockStatic(ThemeQueries.class)) {
            mocked.when(() -> ThemeQueries.getThemes(eq(schemeFilter), eq(datasetsThemeGraph), eq(themesConceptSchemeFilter)))
                    .thenReturn(expectedSparql);
            when(repositoryGestion.getResponseAsArray(expectedSparql)).thenReturn(expected);

            JSONArray result = service.getThemes(schemeFilter);

            assertEquals(expected.toString(), result.toString());
            mocked.verify(() -> ThemeQueries.getThemes(eq(schemeFilter), eq(datasetsThemeGraph), eq(themesConceptSchemeFilter)));
            verify(repositoryGestion).getResponseAsArray(expectedSparql);
            verifyNoMoreInteractions(repositoryGestion);
        }
    }

    @Test
    void getThemes_propagates_RmesException_fromRepository() throws Exception {
        RepositoryGestion repositoryGestion = mock(RepositoryGestion.class);
        ThemeServiceImpl service = new ThemeServiceImpl("g", "f", repositoryGestion);

        String schemeFilter = "any";
        String sparql = "SPARQL";
        RmesException expectedError = new RmesException(500, "boom", "details");

        try (MockedStatic<ThemeQueries> mocked = mockStatic(ThemeQueries.class)) {
            mocked.when(() -> ThemeQueries.getThemes(eq(schemeFilter), eq("g"), eq("f"))).thenReturn(sparql);
            when(repositoryGestion.getResponseAsArray(sparql)).thenThrow(expectedError);

            RmesException thrown = assertThrows(RmesException.class, () -> service.getThemes(schemeFilter));
            assertSame(expectedError, thrown);
            mocked.verify(() -> ThemeQueries.getThemes(eq(schemeFilter), eq("g"), eq("f")));
            verify(repositoryGestion).getResponseAsArray(sparql);
        }
    }
}