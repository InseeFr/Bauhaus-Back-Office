package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.bauhaus_services.themes.ThemeService;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/infrastructure/webservice/ThemeResourcesTest.java
=======
import fr.insee.rmes.onion.infrastructure.webservice.ThemeResources;
>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/test/java/fr/insee/rmes/infrastructure/webservice/ThemeResourcesTest.java
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