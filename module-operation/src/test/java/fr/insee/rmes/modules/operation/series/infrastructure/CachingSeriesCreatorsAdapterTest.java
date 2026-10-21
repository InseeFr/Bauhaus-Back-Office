package fr.insee.rmes.modules.operation.series.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCache;

class CachingSeriesCreatorsAdapterTest {

    private static final String S1 = "http://id.insee.fr/operations/serie/s1";
    private static final String S2 = "http://id.insee.fr/operations/serie/s2";

    private final SeriesCreatorsPort delegate = mock(SeriesCreatorsPort.class);
    private final CachingSeriesCreatorsAdapter adapter =
            new CachingSeriesCreatorsAdapter(delegate, new ConcurrentMapCache("seriesCreators"));

    @Test
    void should_query_graphdb_once_for_series_already_read() {
        when(delegate.getCreatorsForSeries(Set.of(S1))).thenReturn(Map.of(S1, List.of("HIE1")));

        adapter.getCreatorsForSeries(List.of(S1));
        Map<String, List<String>> creators = adapter.getCreatorsForSeries(List.of(S1));

        assertThat(creators).containsExactly(Map.entry(S1, List.of("HIE1")));
        verify(delegate, times(1)).getCreatorsForSeries(Set.of(S1));
    }

    @Test
    void should_query_only_the_series_missing_from_the_cache() {
        when(delegate.getCreatorsForSeries(Set.of(S1))).thenReturn(Map.of(S1, List.of("HIE1")));
        when(delegate.getCreatorsForSeries(Set.of(S2))).thenReturn(Map.of(S2, List.of("HIE2")));
        adapter.getCreatorsForSeries(List.of(S1));

        Map<String, List<String>> creators = adapter.getCreatorsForSeries(List.of(S1, S2));

        assertThat(creators).containsOnly(Map.entry(S1, List.of("HIE1")), Map.entry(S2, List.of("HIE2")));
        verify(delegate, times(1)).getCreatorsForSeries(Set.of(S2));
    }

    @Test
    void should_not_cache_a_series_without_creator_since_it_can_hide_a_graphdb_error() {
        when(delegate.getCreatorsForSeries(Set.of(S1))).thenReturn(Map.of());

        adapter.getCreatorsForSeries(List.of(S1));
        adapter.getCreatorsForSeries(List.of(S1));

        verify(delegate, times(2)).getCreatorsForSeries(Set.of(S1));
    }
}
