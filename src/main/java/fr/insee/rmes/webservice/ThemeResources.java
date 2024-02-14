package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.themes.ThemeService;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/themes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = Constants.DOCUMENT, description = "Themes API")
public class ThemeResources {

    final ThemeService themeService;

    public ThemeResources(ThemeService themeService) {
        this.themeService = themeService;
    }


    @GetMapping(produces = "application/json")
    @Operation(operationId = "getThemes", summary = "List of themes")
    public String getThemes(@RequestParam(required = false) @Parameter(name = "schemeFilter", schema = @Schema(description = "Comma separated list of schemes",type = "string", allowableValues = {"classificationOfStatisticalDomain", "inseeTheme"})) String schemeFilter) throws RmesException {
        return this.themeService.getThemes(schemeFilter).toString();
    }

}