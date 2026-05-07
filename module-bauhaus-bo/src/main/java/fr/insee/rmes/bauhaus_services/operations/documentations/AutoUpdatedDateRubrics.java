package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.model.operations.documentations.DocumentationRubric;

import java.util.List;
import java.util.Set;

public final class AutoUpdatedDateRubrics {

    public static final String DCTERMS_MODIFIED = "http://purl.org/dc/terms/modified";

    private AutoUpdatedDateRubrics() {}

    public static void applyDate(List<DocumentationRubric> rubrics, Set<String> autoUpdatedIds, String isoDate) {
        if (rubrics == null || autoUpdatedIds == null || autoUpdatedIds.isEmpty()) return;
        for (DocumentationRubric rubric : rubrics) {
            String id = rubric.getIdAttribute();
            if (id != null && autoUpdatedIds.contains(id.toUpperCase())) {
                rubric.setValue(List.of(isoDate));
            }
        }
    }
}
