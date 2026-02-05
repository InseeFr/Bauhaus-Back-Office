package fr.insee.rmes.modules.commons.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.themes.ThemeService;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/themes")
public class ThemeResources {

    final ThemeService themeService;

    public ThemeResources(ThemeService themeService) {
        this.themeService = themeService;
    }


    @GetMapping(produces = "application/json")
    public String getThemes(@RequestParam(required = false) String schemeFilter) throws RmesException {
        return this.themeService.getThemes(schemeFilter).toString();
    }

}