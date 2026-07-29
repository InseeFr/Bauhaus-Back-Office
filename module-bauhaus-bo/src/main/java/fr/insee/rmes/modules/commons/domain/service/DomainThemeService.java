package fr.insee.rmes.modules.commons.domain.service;

import fr.insee.rmes.modules.commons.domain.exceptions.ThemeFetchException;
import fr.insee.rmes.modules.commons.domain.model.Theme;
import fr.insee.rmes.modules.commons.domain.port.clientside.ThemeService;
import fr.insee.rmes.modules.commons.domain.port.serverside.ThemeRepository;
import java.util.List;

public class DomainThemeService implements ThemeService {

    private final ThemeRepository themeRepository;

    public DomainThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Override
    public List<Theme> getThemes() throws ThemeFetchException {
        return themeRepository.getThemes();
    }
}
