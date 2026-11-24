package fr.insee.rmes.modules.operation.series.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ServerSidePort
public interface SeriesCreatorsPort {

    /**
     * Returns a map from each series IRI to its list of dc:creator values.
     * A single SPARQL query is used for all IRIs (VALUES batch).
     * IRIs with no creators are absent from the result map.
     */
    Map<String, List<String>> getCreatorsForSeries(Collection<String> seriesIris);
}
