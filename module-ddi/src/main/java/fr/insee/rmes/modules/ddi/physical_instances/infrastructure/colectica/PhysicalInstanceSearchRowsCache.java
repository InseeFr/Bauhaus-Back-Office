package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKey;

/**
 * Mise à jour en place de la région {@link ColecticaCacheNames#PHYSICAL_INSTANCE_SEARCH_ROWS} après
 * la sauvegarde d'une PhysicalInstance dont les parents ne changent pas : libellé et {@code versionDate}
 * sont repris de la PI telle qu'enregistrée, sans aucun appel Colectica, au lieu de vider la région et
 * de refaire la descente Group → StudyUnit → PI (un appel par groupe et par StudyUnit).
 *
 * <p>La date est celle que Bauhaus pose dans le XML ; Colectica date l'enregistrement une à deux
 * secondes plus tard, et c'est cette dernière que la prochaine reconstruction complète affichera.
 *
 * <p>Le cache est manipulé directement, et non par annotation : il faut relire la valeur courante pour
 * la modifier, ce que {@code @CachePut} ne permet pas.
 */
class PhysicalInstanceSearchRowsCache {

    /** Clé posée par {@code @Cacheable} sur {@code getPhysicalInstanceSearchRows()}, sans argument. */
    private static final Object KEY = SimpleKey.EMPTY;

    private final Cache cache;
    private final String defaultLang;

    PhysicalInstanceSearchRowsCache(Cache cache, String defaultLang) {
        this.cache = cache;
        this.defaultLang = defaultLang;
    }

    /**
     * Reporte libellé et date de {@code saved} sur les lignes de la PI. Région vide : rien à faire, la
     * prochaine lecture la construira. PI absente du cache, ou sans libellé : la région est vidée, faute
     * de pouvoir la corriger.
     */
    @SuppressWarnings("unchecked")
    void refresh(String agencyId, String id, Ddi4PhysicalInstance saved) {
        Cache.ValueWrapper cached = cache.get(KEY);
        if (cached == null || cached.get() == null) {
            return;
        }
        List<PhysicalInstanceSearchRow> rows = (List<PhysicalInstanceSearchRow>) cached.get();
        Optional<String> label = labelOf(saved);
        if (label.isEmpty() || rows.stream().noneMatch(row -> isRowOf(row, agencyId, id))) {
            clear();
            return;
        }
        Date versionDate = versionDateOf(saved);
        cache.put(
                KEY,
                rows.stream()
                        .map(row -> isRowOf(row, agencyId, id) ? withLabelAndDate(row, label.get(), versionDate) : row)
                        .toList());
    }

    void clear() {
        cache.clear();
    }

    /** Titre dans la langue par défaut, sinon le premier titre non vide (même règle que la reconstruction). */
    private Optional<String> labelOf(Ddi4PhysicalInstance saved) {
        if (saved == null || saved.citation() == null || saved.citation().title() == null) {
            return Optional.empty();
        }
        List<LangString> titles = saved.citation().title().stream()
                .filter(title -> title.value() != null && !title.value().isBlank())
                .toList();
        return titles.stream()
                .filter(title -> defaultLang.equals(title.language()))
                .findFirst()
                .or(() -> titles.stream().findFirst())
                .map(LangString::value);
    }

    private static Date versionDateOf(Ddi4PhysicalInstance saved) {
        CogsDate versionDate = saved.versionDate();
        return versionDate == null ? null : ColecticaDates.parseInstant(versionDate.dateTime());
    }

    private static boolean isRowOf(PhysicalInstanceSearchRow row, String agencyId, String id) {
        return agencyId.equals(row.agency()) && id.equals(row.id());
    }

    private static PhysicalInstanceSearchRow withLabelAndDate(
            PhysicalInstanceSearchRow row, String label, Date versionDate) {
        return new PhysicalInstanceSearchRow(
                row.agency(),
                row.id(),
                label,
                versionDate,
                row.studyUnitAgency(),
                row.studyUnitId(),
                row.studyUnitLabel(),
                row.groupAgency(),
                row.groupId(),
                row.groupLabel());
    }
}
