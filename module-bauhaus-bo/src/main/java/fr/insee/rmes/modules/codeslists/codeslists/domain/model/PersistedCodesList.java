package fr.insee.rmes.modules.codeslists.codeslists.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.jspecify.annotations.Nullable;

/**
 * Ce que la base sait déjà d'une liste de codes, et que le client ne peut pas décider lui-même :
 * sa date de création et son état de publication.
 */
public record PersistedCodesList(@Nullable String created, ValidationStatus validationState) {
}
