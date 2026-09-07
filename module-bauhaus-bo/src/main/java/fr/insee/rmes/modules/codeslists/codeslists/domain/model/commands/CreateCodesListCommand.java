package fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands;

import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.InvalidCodesListCommandException;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Demande de création d'une liste de codes complète.
 * <p>
 * Les huit champs obligatoires sont ceux que le front exige déjà côté client. Ils étaient
 * auparavant lus sans garde à l'écriture — {@code lastCodeUriSegment} en particulier, dont
 * l'absence ne se voyait qu'au premier ajout de code, sous forme de 500.
 */
public record CreateCodesListCommand(
        String id,
        String labelLg1,
        String labelLg2,
        @Nullable String descriptionLg1,
        @Nullable String descriptionLg2,
        String creator,
        List<String> contributors,
        String disseminationStatus,
        String lastListUriSegment,
        String lastClassUriSegment,
        String lastCodeUriSegment) {

    public CreateCodesListCommand {
        requireNotBlank(id, "id");
        requireNotBlank(labelLg1, "labelLg1");
        requireNotBlank(labelLg2, "labelLg2");
        requireNotBlank(creator, "creator");
        requireNotBlank(disseminationStatus, "disseminationStatus");
        requireNotBlank(lastListUriSegment, "lastListUriSegment");
        requireNotBlank(lastClassUriSegment, "lastClassUriSegment");
        requireNotBlank(lastCodeUriSegment, "lastCodeUriSegment");
        contributors = contributors == null ? List.of() : List.copyOf(contributors);
    }

    private static void requireNotBlank(@Nullable String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidCodesListCommandException("The " + field + " of the codes list is blank");
        }
    }
}
