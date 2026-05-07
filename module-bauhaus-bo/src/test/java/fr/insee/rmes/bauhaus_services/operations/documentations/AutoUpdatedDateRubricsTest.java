package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoUpdatedDateRubricsTest {

    private static DocumentationRubric rubric(String idAttribute, String value) {
        DocumentationRubric r = new DocumentationRubric();
        r.setIdAttribute(idAttribute);
        if (value != null) r.setValue(List.of(value));
        return r;
    }

    @Test
    void replaces_value_of_rubrics_whose_id_is_flagged() {
        DocumentationRubric a = rubric("S.2.3", "old");
        DocumentationRubric b = rubric("S.2.4", "kept");

        AutoUpdatedDateRubrics.applyDate(List.of(a, b), Set.of("S.2.3"), "2026-05-07");

        assertEquals(List.of("2026-05-07"), a.getValue());
        assertEquals(List.of("kept"), b.getValue());
    }

    @Test
    void is_case_insensitive_on_idAttribute() {
        DocumentationRubric a = rubric("s.2.3", "old");

        AutoUpdatedDateRubrics.applyDate(List.of(a), Set.of("S.2.3"), "2026-05-07");

        assertEquals(List.of("2026-05-07"), a.getValue());
    }

    @Test
    void does_nothing_when_no_id_is_flagged() {
        DocumentationRubric a = rubric("S.1", "kept");

        AutoUpdatedDateRubrics.applyDate(List.of(a), Set.of(), "2026-05-07");

        assertEquals(List.of("kept"), a.getValue());
    }

    @Test
    void writes_value_when_rubric_has_no_existing_value() {
        DocumentationRubric a = rubric("S.2.3", null);

        AutoUpdatedDateRubrics.applyDate(List.of(a), Set.of("S.2.3"), "2026-05-07");

        assertEquals(List.of("2026-05-07"), a.getValue());
    }

    @Test
    void tolerates_null_rubrics() {
        AutoUpdatedDateRubrics.applyDate(null, Set.of("S.2.3"), "2026-05-07");
    }
}
