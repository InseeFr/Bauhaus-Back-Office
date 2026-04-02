package fr.insee.rmes.modules.commons.domain.port.clientside;

import fr.insee.rmes.modules.commons.domain.exceptions.ThemeFetchException;
import fr.insee.rmes.modules.commons.domain.model.Theme;
import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;

import java.util.List;

@ClientSidePort
public interface ThemeService {
    List<Theme> getThemes(String schemeFilter) throws ThemeFetchException;
}