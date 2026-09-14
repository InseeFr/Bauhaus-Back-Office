package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Common interface for DDI4 item types (Group, StudyUnit, PhysicalInstance, etc.).
 * <p>
 * Provides access to the shared metadata fields that all DDI items have in common,
 * following the DDI Lifecycle 3.3 reusable module structure.
 * <p>
 * Comme {@link Ddi4VersionedItem}, ces items voyagent dans le tableau {@code items} de
 * l'enveloppe du schéma et s'y distinguent par leur {@code $type}, déjà porté par chaque record
 * (d'où {@code EXISTING_PROPERTY}).
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "$type",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Ddi4Group.class, name = Ddi4Group.TYPE),
    @JsonSubTypes.Type(value = Ddi4StudyUnit.class, name = Ddi4StudyUnit.TYPE),
    @JsonSubTypes.Type(value = Ddi4PhysicalInstance.class, name = Ddi4PhysicalInstance.TYPE)
})
public interface Ddi4Item {
    String type();

    CogsDate versionDate();

    String urn();

    String agency();

    String id();

    String version();

    Citation citation();
}
