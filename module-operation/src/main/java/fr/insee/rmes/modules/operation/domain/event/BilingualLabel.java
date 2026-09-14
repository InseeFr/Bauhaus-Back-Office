package fr.insee.rmes.modules.operation.domain.event;

/**
 * Un libellé dans les deux langues de Bauhaus, dans l'ordre {@code lg1} / {@code lg2} fixé par la
 * configuration de l'application. Les événements du domaine « opérations » transportent les libellés
 * sous cette forme positionnelle plutôt qu'avec des codes langue : c'est la forme qu'ils ont déjà en
 * RDF, et elle laisse à chaque consommateur le soin de la traduire dans son propre vocabulaire de
 * langues.
 *
 * @param lg2 peut être {@code null} : un libellé n'est pas toujours saisi dans la seconde langue.
 */
public record BilingualLabel(String lg1, String lg2) {

    public boolean isEmpty() {
        return isBlank(lg1) && isBlank(lg2);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
