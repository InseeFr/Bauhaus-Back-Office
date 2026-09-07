package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidDdi4JsonException;

import java.util.List;

/**
 * Confrontation d'un document au JSON Schema DDI 4.
 * <p>
 * Port serveur et non service de domaine : la validation JSON Schema est portée par une bibliothèque
 * tierce, que le domaine n'a pas le droit d'importer.
 */
@ServerSidePort
public interface Ddi4SchemaValidator {

    /**
     * @return les messages d'erreur du schéma, vide si le document est conforme
     * @throws InvalidDdi4JsonException si {@code json} n'est pas du JSON bien formé
     */
    List<String> validate(String json);
}
