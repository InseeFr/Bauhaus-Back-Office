package fr.insee.rmes.modules.commons.webservice;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.domain.exceptions.ThemeFetchException;
import fr.insee.rmes.modules.commons.domain.model.Theme;
import fr.insee.rmes.modules.commons.domain.port.clientside.ThemeService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/themes")
public class ThemeResources {

    final ThemeService themeService;

    public ThemeResources(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping(produces = "application/json")
    public List<Theme> getThemes() throws RmesException {
        try {
            return this.themeService.getThemes();
        } catch (ThemeFetchException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
