package fr.insee.rmes.bauhaus_services.themes;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.baseGraph=http://"})
class ThemeQueriesTest {
    @Autowired
    Config config;

    @Test
    void shouldCallGetThemesQueryWithNonDefaultSchemeFilters() throws RmesException {
        ThemeQueries.setConfig(config);

        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CONCEPTS_GRAPH", "http://concepts-graph");
                put("LG1", "fr");
                put("CONCEPT_SCHEME_FILTER", "filter1,filter2");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("theme/"), eq("getThemes.ftlh"), eq(map))).thenReturn("request");
            String query = ThemeQueries.getThemes("filter1,filter2", "concepts-graph", "filter3");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetThemesQueryWithDefaultSchemeFilters() throws RmesException {
        ThemeQueries.setConfig(config);

        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CONCEPTS_GRAPH", "http://concepts-graph");
                put("LG1", "fr");
                put("CONCEPT_SCHEME_FILTER", "filter3");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("theme/"), eq("getThemes.ftlh"), eq(map))).thenReturn("request");
            String query = ThemeQueries.getThemes(null, "concepts-graph", "filter3");
            Assertions.assertEquals("request", query);
        }
    }
}