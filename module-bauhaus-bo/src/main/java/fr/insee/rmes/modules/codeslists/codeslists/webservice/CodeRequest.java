package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Corps des requêtes de création et de mise à jour d'un code d'une liste de codes.
 * <p>
 * La charge utile est fermée : l'écriture RDF ne pose que ces champs, le reste de ce que le
 * front peut renvoyer (iri, codeUri, closeMatch...) est ignoré à la désérialisation.
 * <p>
 * Les trois champs obligatoires étaient auparavant lus sans garde côté service : un corps
 * incomplet remontait en 500 au lieu du 400 contractuel.
 * <p>
 * {@code broader} et {@code narrower} portent les notations des codes parents et enfants, pris
 * dans la même liste. Absents, ils valent une liste vide : le code n'a alors plus aucun lien.
 */
public record CodeRequest(
        @NotBlank(message = "code is required") String code,
        @NotBlank(message = "labelLg1 is required") String labelLg1,
        @NotBlank(message = "labelLg2 is required") String labelLg2,
        String descriptionLg1,
        String descriptionLg2,
        List<String> broader,
        List<String> narrower) {

    public CodeRequest {
        broader = broader == null ? List.of() : List.copyOf(broader);
        narrower = narrower == null ? List.of() : List.copyOf(narrower);
    }

    public CodeRequest(String code, String labelLg1, String labelLg2, String descriptionLg1, String descriptionLg2) {
        this(code, labelLg1, labelLg2, descriptionLg1, descriptionLg2, List.of(), List.of());
    }
}
