package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands.CreateCodesListCommand;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands.UpdateCodesListCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Corps des requêtes de création et de mise à jour d'une liste de codes complète.
 * <p>
 * Les huit champs {@code @NotBlank} sont exactement ceux que la validation zod du front impose déjà
 * côté client. Le contrôle historique ({@code validateCodeList}) ne testait que la présence de la
 * clé avec {@code JSONObject.has()} : {@code {"labelLg1": " "}} passait. Et
 * {@code lastCodeUriSegment}, {@code creator} et {@code disseminationStatus} n'étaient contrôlés
 * nulle part, alors qu'ils sont lus sans garde à l'écriture — l'absence du premier ne se voyait
 * qu'au premier ajout de code sur la liste, sous forme de 500.
 * <p>
 * La charge utile est fermée : le front renvoie la liste entière telle qu'il l'a lue
 * ({@code iri}, {@code created}, {@code validationState}...), tout ce qui n'est pas déclaré ici est
 * ignoré à la désérialisation. {@code created} et {@code validationState} en particulier
 * n'appartiennent pas au client : ils sont relus en base à la mise à jour.
 *
 * @param codes les codes portés par le corps ne sont pas écrits par cet endpoint (ils passent par
 *              {@code /detailed/{id}/codes}), mais s'ils sont là ils doivent être complets.
 */
public record CodesListRequest(
        @NotBlank(message = "id is required") String id,
        @NotBlank(message = "labelLg1 is required") String labelLg1,
        @NotBlank(message = "labelLg2 is required") String labelLg2,
        String descriptionLg1,
        String descriptionLg2,
        @NotBlank(message = "creator is required") String creator,
        List<String> contributor,

        @NotBlank(message = "disseminationStatus is required")
        String disseminationStatus,

        @NotBlank(message = "lastListUriSegment is required")
        String lastListUriSegment,

        @NotBlank(message = "lastClassUriSegment is required")
        String lastClassUriSegment,

        @NotBlank(message = "lastCodeUriSegment is required")
        String lastCodeUriSegment,

        List<@Valid CodeRequest> codes) {

    public CreateCodesListCommand toCreateCommand() {
        return new CreateCodesListCommand(
                id,
                labelLg1,
                labelLg2,
                descriptionLg1,
                descriptionLg2,
                creator,
                contributor,
                disseminationStatus,
                lastListUriSegment,
                lastClassUriSegment,
                lastCodeUriSegment);
    }

    public UpdateCodesListCommand toUpdateCommand() {
        return new UpdateCodesListCommand(toCreateCommand());
    }
}
