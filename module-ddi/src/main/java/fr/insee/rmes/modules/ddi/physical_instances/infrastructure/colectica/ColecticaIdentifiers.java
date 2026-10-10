package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import java.util.regex.Pattern;

/**
 * Colectica n'identifie ses items que par des UUID : un autre identifiant ne peut désigner aucun
 * item. Mieux vaut ne pas l'interroger, il y répond par une erreur (400 sur {@code set/}, 500 sur
 * {@code ddiset/}) qui passerait pour une panne.
 */
final class ColecticaIdentifiers {

    private static final Pattern UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private ColecticaIdentifiers() {}

    static boolean isIdentifier(String id) {
        return id != null && UUID.matcher(id).matches();
    }
}
