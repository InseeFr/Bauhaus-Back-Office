package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import jakarta.validation.constraints.NotBlank;

/**
 * Corps des requêtes de création et de mise à jour d'un code d'une liste de codes.
 * <p>
 * La charge utile est fermée : l'écriture RDF ne pose que ces cinq champs, le reste de ce que le
 * front peut renvoyer (iri, codeUri, broader...) est ignoré à la désérialisation.
 * <p>
 * Les trois champs obligatoires étaient auparavant lus sans garde côté service : un corps
 * incomplet remontait en 500 au lieu du 400 contractuel.
 */
public record CodeRequest(
        @NotBlank(message = "code is required") String code,
        @NotBlank(message = "labelLg1 is required") String labelLg1,
        @NotBlank(message = "labelLg2 is required") String labelLg2,
        String descriptionLg1,
        String descriptionLg2) {
}
