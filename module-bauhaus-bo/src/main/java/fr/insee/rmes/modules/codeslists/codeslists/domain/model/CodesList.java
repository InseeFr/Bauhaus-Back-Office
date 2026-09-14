package fr.insee.rmes.modules.codeslists.codeslists.domain.model;

import fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands.CreateCodesListCommand;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Une liste de codes complète, telle qu'elle sera écrite.
 *
 * @param created null à la création : c'est l'infrastructure qui horodate. À la mise à jour, on y
 *                remet la date lue en base — elle n'appartient pas au client.
 */
public record CodesList(
        CodesListId id,
        String labelLg1,
        String labelLg2,
        @Nullable String descriptionLg1,
        @Nullable String descriptionLg2,
        String creator,
        List<String> contributors,
        String disseminationStatus,
        String lastListUriSegment,
        String lastClassUriSegment,
        String lastCodeUriSegment,
        ValidationStatus validationState,
        @Nullable String created) {

    public CodesList {
        contributors = contributors == null ? List.of() : List.copyOf(contributors);
    }

    public static CodesList create(CreateCodesListCommand command) {
        return of(command, ValidationStatus.UNPUBLISHED, null);
    }

    /**
     * Une liste déjà publiée repasse en « Modified », pas en « Unpublished » : l'état de publication
     * se lit en base et non dans le corps de la requête, que le client maîtrise.
     */
    public static CodesList revise(CreateCodesListCommand command, PersistedCodesList persisted) {
        ValidationStatus next =
                switch (persisted.validationState()) {
                    case VALIDATED, MODIFIED -> ValidationStatus.MODIFIED;
                    case UNPUBLISHED -> ValidationStatus.UNPUBLISHED;
                };
        return of(command, next, persisted.created());
    }

    private static CodesList of(
            CreateCodesListCommand command, ValidationStatus validationState, @Nullable String created) {
        return new CodesList(
                new CodesListId(command.id()),
                command.labelLg1(),
                command.labelLg2(),
                command.descriptionLg1(),
                command.descriptionLg2(),
                command.creator(),
                command.contributors(),
                command.disseminationStatus(),
                command.lastListUriSegment(),
                command.lastClassUriSegment(),
                command.lastCodeUriSegment(),
                validationState,
                created);
    }
}
