package fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions;

/**
 * L'identifiant de l'url ne désigne pas la liste décrite par le corps. Bean Validation ne peut pas
 * porter cette règle : elle croise le corps et la {@code @PathVariable}.
 */
public class CodesListIdMismatchException extends Exception {
    public CodesListIdMismatchException(String message) {
        super(message);
    }
}
