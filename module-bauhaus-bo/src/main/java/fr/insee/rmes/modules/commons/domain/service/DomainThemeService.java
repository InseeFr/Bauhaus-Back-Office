package fr.insee.rmes.modules.commons.domain.service;

import fr.insee.rmes.modules.commons.domain.exceptions.ThemeFetchException;
import fr.insee.rmes.modules.commons.domain.model.Theme;
import fr.insee.rmes.modules.commons.domain.port.clientside.ThemeService;
import fr.insee.rmes.modules.commons.domain.port.serverside.ThemeRepository;

import java.util.List;

public class DomainThemeService implements ThemeService {

    private final ThemeRepository themeRepository;
    private final String defaultConceptSchemeFilter;

    public DomainThemeService(ThemeRepository themeRepository, String defaultConceptSchemeFilter) {
        this.themeRepository = themeRepository;
        this.defaultConceptSchemeFilter = defaultConceptSchemeFilter;
    }

    @Override
    public List<Theme> getThemes(String schemeFilter) throws ThemeFetchException {
        String filter = (schemeFilter != null && !schemeFilter.isBlank()) ? schemeFilter : defaultConceptSchemeFilter;
        return themeRepository.getThemes(filter);
    }
}