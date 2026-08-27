package fr.insee.rmes.modules.operations.families.webservice;

import fr.insee.rmes.modules.operations.families.domain.model.commands.CreateFamilyCommand;
import fr.insee.rmes.modules.operations.families.domain.model.commands.UpdateFamilyCommand;
import jakarta.validation.constraints.NotBlank;

/**
 * Corps des requêtes de création et de mise à jour d'une famille.
 * <p>
 * Le front poste la famille entière telle qu'il l'a chargée : les champs qu'il renvoie sans les
 * éditer ({@code id}, {@code validationState}, {@code modified}, {@code series}...) sont ignorés à
 * la désérialisation. {@code created} fait exception : l'écriture RDF remplace tous les triplets de
 * la famille, la date de création serait perdue si elle n'était pas relue du corps de la requête.
 * <p>
 * L'identifiant d'une mise à jour vient du chemin, jamais du corps.
 */
public record FamilyRequest(
        @NotBlank(message = "prefLabelLg1 is required") String prefLabelLg1,
        @NotBlank(message = "prefLabelLg2 is required") String prefLabelLg2,
        String abstractLg1,
        String abstractLg2,
        String created) {

    CreateFamilyCommand toCreateCommand() {
        return new CreateFamilyCommand(prefLabelLg1, prefLabelLg2, abstractLg1, abstractLg2);
    }

    UpdateFamilyCommand toUpdateCommand(String id) {
        return new UpdateFamilyCommand(id, prefLabelLg1, prefLabelLg2, abstractLg1, abstractLg2, created);
    }
}
