package fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Demande de mise à jour. Même charge utile que la création : le front renvoie la liste entière,
 * et c'est le service qui décide de ce qui n'appartient pas au client (date de création, état de
 * publication).
 */
public record UpdateCodesListCommand(CreateCodesListCommand attributes) {

    public UpdateCodesListCommand(
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
        this(new CreateCodesListCommand(id, labelLg1, labelLg2, descriptionLg1, descriptionLg2, creator,
                contributors, disseminationStatus, lastListUriSegment, lastClassUriSegment, lastCodeUriSegment));
    }

    public String id() {
        return attributes.id();
    }

    public String lastListUriSegment() {
        return attributes.lastListUriSegment();
    }
}
