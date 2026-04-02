package fr.insee.rmes.modules.commons.domain.port.serverside;

import fr.insee.rmes.modules.commons.domain.exceptions.ThemeFetchException;
import fr.insee.rmes.modules.commons.domain.model.Theme;
import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;

import java.util.List;

@ServerSidePort
public interface ThemeRepository {
    List<Theme> getThemes(String conceptSchemeFilter) throws ThemeFetchException;
}