package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Item DDI 4 versionné porté par une {@link Ddi4Response} (PhysicalInstance,
 * DataRelationship, Variable, CodeList, Category).
 * <p>
 * Contrat minimal nécessaire à la réconciliation des {@code VersionDate} au PUT :
 * identité ({@code agency} + {@code id}) et réécriture de la date. Distinct de
 * {@link Ddi4Item}, réservé aux items à {@code Citation} (Group, StudyUnit).
 * <p>
 * Le {@code $type} qui discrimine ces items dans le tableau {@code items} de l'enveloppe est
 * déclaré {@code EXISTING_PROPERTY} : chaque record le porte déjà comme composant, Jackson le
 * lit pour choisir la classe à l'entrée sans le réécrire à la sortie.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "$type",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Ddi4PhysicalInstance.class, name = Ddi4PhysicalInstance.TYPE),
    @JsonSubTypes.Type(value = Ddi4DataRelationship.class, name = Ddi4DataRelationship.TYPE),
    @JsonSubTypes.Type(value = Ddi4Variable.class, name = Ddi4Variable.TYPE),
    @JsonSubTypes.Type(value = Ddi4CodeList.class, name = Ddi4CodeList.TYPE),
    @JsonSubTypes.Type(value = Ddi4Category.class, name = Ddi4Category.TYPE),
    @JsonSubTypes.Type(
            value = Ddi4ManagedMissingValuesRepresentation.class,
            name = Ddi4ManagedMissingValuesRepresentation.TYPE)
})
public interface Ddi4VersionedItem {
    String agency();

    String id();

    CogsDate versionDate();

    /** Copie de l'item avec la {@code VersionDate} donnée, le reste inchangé. */
    Ddi4VersionedItem withVersionDate(CogsDate versionDate);
}
