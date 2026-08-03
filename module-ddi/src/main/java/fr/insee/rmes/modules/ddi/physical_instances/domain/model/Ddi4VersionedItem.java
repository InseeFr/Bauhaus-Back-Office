package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

/**
 * Item DDI 4 versionné porté par une {@link Ddi4Response} (PhysicalInstance,
 * DataRelationship, Variable, CodeList, Category).
 * <p>
 * Contrat minimal nécessaire à la réconciliation des {@code VersionDate} au PUT :
 * identité ({@code agency} + {@code id}) et réécriture de la date. Distinct de
 * {@link Ddi4Item}, réservé aux items à {@code Citation} (Group, StudyUnit).
 */
public interface Ddi4VersionedItem {
    String agency();
    String id();
    CogsDate versionDate();

    /** Copie de l'item avec la {@code VersionDate} donnée, le reste inchangé. */
    Ddi4VersionedItem withVersionDate(CogsDate versionDate);
}
