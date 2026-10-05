package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.SimpleKey;

class PhysicalInstanceSearchRowsCacheTest {

    private final Cache cache = new ConcurrentMapCache(ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS);

    private final PhysicalInstanceSearchRowsCache searchRowsCache = new PhysicalInstanceSearchRowsCache(cache, "fr-FR");

    @Test
    void refresh_leavesAnEmptyRegionEmpty() {
        searchRowsCache.refresh("fr.insee", "pi-1", savedWithTitles(new LangString("fr-FR", "New PI")));

        assertThat(cache.get(SimpleKey.EMPTY)).isNull();
    }

    @Test
    void refresh_leavesARegionHoldingNullUntouched() {
        cache.put(SimpleKey.EMPTY, null);

        searchRowsCache.refresh("fr.insee", "pi-1", savedWithTitles(new LangString("fr-FR", "New PI")));

        assertThat(cache.get(SimpleKey.EMPTY)).isNotNull();
        assertThat(cache.get(SimpleKey.EMPTY).get()).isNull();
    }

    @Test
    void refresh_copiesLabelAndVersionDateOntoTheRowsOfThePhysicalInstanceOnly() {
        cache.put(
                SimpleKey.EMPTY,
                List.of(
                        row("fr.insee", "pi-1", "Old PI"),
                        row("fr.insee", "pi-2", "Other"),
                        row("other", "pi-1", "Other")));

        searchRowsCache.refresh("fr.insee", "pi-1", savedWithTitles(new LangString("fr-FR", "New PI")));

        assertThat(rows()).extracting(PhysicalInstanceSearchRow::label).containsExactly("New PI", "Other", "Other");
        assertThat(rows().getFirst().versionDate()).isEqualTo(Date.from(Instant.parse("2026-10-02T10:00:00Z")));
        assertThat(rows().getFirst().studyUnitLabel()).isEqualTo("Recensement 2024");
        assertThat(rows().get(1).versionDate()).isNull();
    }

    @Test
    void refresh_prefersTheNonBlankTitleInTheDefaultLanguage() {
        cache.put(SimpleKey.EMPTY, List.of(row("fr.insee", "pi-1", "Old PI")));

        searchRowsCache.refresh(
                "fr.insee",
                "pi-1",
                savedWithTitles(
                        new LangString("en-GB", "English PI"),
                        new LangString("fr-FR", null),
                        new LangString("fr-FR", " "),
                        new LangString("fr-FR", "PI française")));

        assertThat(rows()).extracting(PhysicalInstanceSearchRow::label).containsExactly("PI française");
    }

    @Test
    void refresh_fallsBackOnTheFirstNonBlankTitleWithoutTitleInTheDefaultLanguage() {
        cache.put(SimpleKey.EMPTY, List.of(row("fr.insee", "pi-1", "Old PI")));

        searchRowsCache.refresh(
                "fr.insee",
                "pi-1",
                savedWithTitles(new LangString("en-GB", ""), new LangString("en-GB", "English PI")));

        assertThat(rows()).extracting(PhysicalInstanceSearchRow::label).containsExactly("English PI");
    }

    @Test
    void refresh_leavesTheVersionDateEmptyWhenTheSavedPhysicalInstanceHasNone() {
        cache.put(SimpleKey.EMPTY, List.of(row("fr.insee", "pi-1", "Old PI")));

        searchRowsCache.refresh(
                "fr.insee", "pi-1", saved(null, new Citation(List.of(new LangString("fr-FR", "New PI")))));

        assertThat(rows().getFirst().label()).isEqualTo("New PI");
        assertThat(rows().getFirst().versionDate()).isNull();
    }

    @Test
    void refresh_clearsTheRegionWhenThePhysicalInstanceIsNotCached() {
        cache.put(SimpleKey.EMPTY, List.of(row("fr.insee", "pi-2", "Other")));

        searchRowsCache.refresh("fr.insee", "pi-1", savedWithTitles(new LangString("fr-FR", "New PI")));

        assertThat(cache.get(SimpleKey.EMPTY)).isNull();
    }

    @Test
    void refresh_clearsTheRegionWithoutSavedPhysicalInstance() {
        cache.put(SimpleKey.EMPTY, List.of(row("fr.insee", "pi-1", "Old PI")));

        searchRowsCache.refresh("fr.insee", "pi-1", null);

        assertThat(cache.get(SimpleKey.EMPTY)).isNull();
    }

    @Test
    void refresh_clearsTheRegionWhenTheSavedPhysicalInstanceHasNoCitation() {
        cache.put(SimpleKey.EMPTY, List.of(row("fr.insee", "pi-1", "Old PI")));

        searchRowsCache.refresh("fr.insee", "pi-1", saved(CogsDate.ofDateTime("2026-10-02T10:00:00"), null));

        assertThat(cache.get(SimpleKey.EMPTY)).isNull();
    }

    @Test
    void refresh_clearsTheRegionWhenTheSavedPhysicalInstanceHasNoTitle() {
        cache.put(SimpleKey.EMPTY, List.of(row("fr.insee", "pi-1", "Old PI")));

        searchRowsCache.refresh(
                "fr.insee", "pi-1", saved(CogsDate.ofDateTime("2026-10-02T10:00:00"), new Citation(null)));

        assertThat(cache.get(SimpleKey.EMPTY)).isNull();
    }

    @Test
    void refresh_clearsTheRegionWhenEveryTitleIsBlank() {
        cache.put(SimpleKey.EMPTY, List.of(row("fr.insee", "pi-1", "Old PI")));

        searchRowsCache.refresh(
                "fr.insee", "pi-1", savedWithTitles(new LangString("fr-FR", " "), new LangString("en-GB", null)));

        assertThat(cache.get(SimpleKey.EMPTY)).isNull();
    }

    @SuppressWarnings("unchecked")
    private List<PhysicalInstanceSearchRow> rows() {
        return cache.get(SimpleKey.EMPTY, List.class);
    }

    private static Ddi4PhysicalInstance savedWithTitles(LangString... titles) {
        return saved(CogsDate.ofDateTime("2026-10-02T10:00:00"), new Citation(Arrays.asList(titles)));
    }

    private static Ddi4PhysicalInstance saved(CogsDate versionDate, Citation citation) {
        return new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                versionDate,
                "urn:ddi:fr.insee:pi-1:2",
                "fr.insee",
                "pi-1",
                "2",
                null,
                citation,
                List.of());
    }

    private static PhysicalInstanceSearchRow row(String agency, String id, String label) {
        return new PhysicalInstanceSearchRow(
                agency, id, label, null, "fr.insee", "su-1", "Recensement 2024", "fr.insee", "g1", "Groupe BPE");
    }
}
