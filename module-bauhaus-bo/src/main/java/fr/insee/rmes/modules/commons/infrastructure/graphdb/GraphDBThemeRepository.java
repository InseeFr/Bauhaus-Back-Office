package fr.insee.rmes.modules.commons.infrastructure.graphdb;

import fr.insee.rmes.modules.commons.domain.exceptions.ThemeFetchException;
import fr.insee.rmes.modules.commons.domain.model.Theme;
import fr.insee.rmes.modules.commons.domain.port.serverside.ThemeRepository;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.Deserializer;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@ServerSideAdaptor
@Repository
public class GraphDBThemeRepository implements ThemeRepository {

    private final RepositoryGestion repositoryGestion;

    public GraphDBThemeRepository(RepositoryGestion repositoryGestion) {
        this.repositoryGestion = repositoryGestion;
    }

    @Override
    public List<Theme> getThemes(String conceptSchemeFilter) throws ThemeFetchException {
        try {
            var results = repositoryGestion.getResponseAsArray(ThemeQueries.getThemesQuery(conceptSchemeFilter));
            return Arrays.stream(Deserializer.deserializeJSONArray(results, GraphDBTheme[].class))
                    .map(GraphDBTheme::toDomain)
                    .toList();
        } catch (Exception e) {
            throw new ThemeFetchException(e);
        }
    }
}