package fr.insee.rmes.bauhaus_services.themes;

import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.json.JSONArray;

public interface ThemeService {
    JSONArray getThemes(String schemeFilter) throws RmesException;
}