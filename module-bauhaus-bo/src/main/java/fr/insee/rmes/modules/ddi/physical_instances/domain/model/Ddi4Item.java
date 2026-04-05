package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

/**
 * Common interface for DDI4 item types (Group, StudyUnit, PhysicalInstance, etc.).
 * <p>
 * Provides access to the shared metadata fields that all DDI items have in common,
 * following the DDI Lifecycle 3.3 reusable module structure.
 */
public interface Ddi4Item {
    String isUniversallyUnique();
    String versionDate();
    String urn();
    String agency();
    String id();
    String version();
    Citation citation();
}
