package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import org.jspecify.annotations.Nullable;

/**
 * Une variable de la PI source reprise dans la nouvelle période (#1672) : copiée (nouvel
 * identifiant, {@code BasedOn} vers la source) ou, si {@code reusedId} est renseigné, remplacée par
 * une variable déjà dérivée de la source dans le VariableScheme de l'étude cible.
 */
public record RolloverVariable(
        String sourceId,
        @Nullable String reusedAgency,
        @Nullable String reusedId) {

    public static RolloverVariable copy(String sourceId) {
        return new RolloverVariable(sourceId, null, null);
    }

    public static RolloverVariable reuse(String sourceId, String reusedAgency, String reusedId) {
        return new RolloverVariable(sourceId, reusedAgency, reusedId);
    }

    public boolean reuses() {
        return reusedId != null && !reusedId.isBlank();
    }
}
