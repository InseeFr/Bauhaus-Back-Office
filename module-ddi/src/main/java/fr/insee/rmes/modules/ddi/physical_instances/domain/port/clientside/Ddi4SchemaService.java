package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidDdi4JsonException;

import java.util.List;

/**
 * Ce que l'application sait faire du schéma DDI 4 : le servir, et y confronter un document.
 * <p>
 * Seul point d'entrée du webservice — le chargement du schéma comme la bibliothèque de validation
 * restent derrière les ports serveur.
 */
@ClientSidePort
public interface Ddi4SchemaService {

    /** @return le document JSON Schema, débarrassé de son BOM UTF-8 */
    String schemaDocument();

    /**
     * @return les messages d'erreur du schéma, vide si le document est conforme
     * @throws InvalidDdi4JsonException si {@code json} n'est pas du JSON bien formé
     */
    List<String> validate(String json);
}
