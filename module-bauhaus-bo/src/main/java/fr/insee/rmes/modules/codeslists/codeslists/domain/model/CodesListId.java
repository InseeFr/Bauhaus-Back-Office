package fr.insee.rmes.modules.codeslists.codeslists.domain.model;

import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.InvalidCodesListIdException;

/**
 * Notation d'une liste de codes complète.
 * <p>
 * Ce n'est pas son identité : une liste reste renommable depuis le front, seule son IRI — dérivée
 * de {@code lastListUriSegment} — est stable. Tout contrôle d'existence passe donc par le segment
 * d'URI, jamais par cet identifiant.
 */
public record CodesListId(String value) {

    public CodesListId {
        if (value == null || value.isBlank()) {
            throw new InvalidCodesListIdException("The identifier of the codes list is blank");
        }
    }
}
