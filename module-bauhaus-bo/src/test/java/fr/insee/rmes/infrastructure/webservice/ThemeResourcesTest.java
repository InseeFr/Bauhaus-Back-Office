package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.bauhaus_services.themes.ThemeService;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.infrastructure.webservice.ThemeResources;
import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThemeResourcesTest {
    @InjectMocks
    private ThemeResources themeResources;

    @Mock
    ThemeService themeService;
    @Test
    void shouldReturnThemes() throws RmesException {
        when(themeService.getThemes("filter")).thenReturn(new JSONArray().put("theme"));
        Assertions.assertEquals("[\"theme\"]", themeResources.getThemes("filter"));
    }
}