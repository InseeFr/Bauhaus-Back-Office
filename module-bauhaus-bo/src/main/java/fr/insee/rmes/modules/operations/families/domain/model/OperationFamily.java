package fr.insee.rmes.modules.operations.families.domain.model;

import fr.insee.rmes.modules.operations.families.domain.model.commands.CreateFamilyCommand;
import fr.insee.rmes.modules.operations.families.domain.model.commands.UpdateFamilyCommand;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;

public record OperationFamily(
        String id,
        String prefLabelLg1,
        String prefLabelLg2,
        String abstractLg1,
        String abstractLg2,
        String validationState,
        String created,
        String modified,
        List<OperationFamilySeries> series,
        List<OperationFamilySubject> subjects) {

    /** Famille nouvellement créée : jamais publiée, créée et modifiée à la même seconde. */
    public static OperationFamily create(String id, CreateFamilyCommand command, String creationDate) {
        return new OperationFamily(
                id,
                command.prefLabelLg1(),
                command.prefLabelLg2(),
                command.abstractLg1(),
                command.abstractLg2(),
                ValidationStatus.UNPUBLISHED.getValue(),
                creationDate,
                creationDate,
                Collections.emptyList(),
                Collections.emptyList());
    }

    public static OperationFamily update(
            UpdateFamilyCommand command, ValidationStatus validationStatus, String modificationDate) {
        return new OperationFamily(
                command.id(),
                command.prefLabelLg1(),
                command.prefLabelLg2(),
                command.abstractLg1(),
                command.abstractLg2(),
                validationStatus.getValue(),
                command.created(),
                modificationDate,
                Collections.emptyList(),
                Collections.emptyList());
    }

    public static OperationFamily fromJson(JSONObject obj) {
        String id = obj.optString("id", null);
        String prefLabelLg1 = obj.optString("prefLabelLg1", null);
        String prefLabelLg2 = obj.optString("prefLabelLg2", null);
        String abstractLg1 = obj.optString("abstractLg1", null);
        String abstractLg2 = obj.optString("abstractLg2", null);
        String validationState = obj.optString("validationState", null);
        String created = obj.optString("created", null);
        String modified = obj.optString("modified", null);

        return new OperationFamily(
                id,
                prefLabelLg1,
                prefLabelLg2,
                abstractLg1,
                abstractLg2,
                validationState,
                created,
                modified,
                Collections.emptyList(),
                Collections.emptyList());
    }

    public OperationFamily withSeries(List<OperationFamilySeries> series) {
        return new OperationFamily(
                id,
                prefLabelLg1,
                prefLabelLg2,
                abstractLg1,
                abstractLg2,
                validationState,
                created,
                modified,
                series,
                Collections.emptyList());
    }

    public OperationFamily withSubject(List<OperationFamilySubject> subjects) {
        return new OperationFamily(
                id,
                prefLabelLg1,
                prefLabelLg2,
                abstractLg1,
                abstractLg2,
                validationState,
                created,
                modified,
                series,
                subjects);
    }
}
