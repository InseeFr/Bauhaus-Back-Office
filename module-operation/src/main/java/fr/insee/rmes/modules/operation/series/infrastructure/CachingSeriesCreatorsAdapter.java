package fr.insee.rmes.modules.operation.series.infrastructure;

import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.cache.Cache;

/**
 * Mémoïse les créateurs par série : seules les séries absentes du cache partent en requête.
 *
 * <p>Une série sans créateur n'est pas mise en cache : l'adaptateur GraphDB renvoie aussi une table
 * vide en cas d'erreur, et la conserver masquerait la série jusqu'à expiration.</p>
 */
public class CachingSeriesCreatorsAdapter implements SeriesCreatorsPort {

    private final SeriesCreatorsPort delegate;
    private final Cache cache;

    public CachingSeriesCreatorsAdapter(SeriesCreatorsPort delegate, Cache cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getCreatorsForSeries(Collection<String> seriesIris) {
        Map<String, List<String>> creatorsByIri = new HashMap<>();
        Set<String> missing = new LinkedHashSet<>();
        for (String iri : seriesIris) {
            List<String> cached = cache.get(iri, List.class);
            if (cached != null) {
                creatorsByIri.put(iri, cached);
            } else {
                missing.add(iri);
            }
        }
        if (!missing.isEmpty()) {
            delegate.getCreatorsForSeries(missing).forEach((iri, creators) -> {
                List<String> copy = List.copyOf(creators);
                cache.put(iri, copy);
                creatorsByIri.put(iri, copy);
            });
        }
        return creatorsByIri;
    }
}
