package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

/**
 * A usage of a CodeList: a Variable that references the code list, together with the
 * PhysicalInstance it belongs to and the StudyUnit that owns the PhysicalInstance.
 *
 * <p>Each item is carried as an {@code agencyId/id} pair plus its resolved label (default language
 * with fallback). The StudyUnit fields may be {@code null} when no owning StudyUnit can be resolved.
 * The frontend groups these flat rows into a StudyUnit / PhysicalInstance / Variable tree.
 */
public record CodeListVariableUsage(
        String studyUnitAgencyId,
        String studyUnitId,
        String studyUnitLabel,
        String physicalInstanceAgencyId,
        String physicalInstanceId,
        String physicalInstanceLabel,
        String variableAgencyId,
        String variableId,
        String variableLabel) {}
