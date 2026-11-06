package fr.insee.rmes.modules.operations.documents.domain.port.serverside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import java.util.List;

/** Timbres des propriétaires d'un rapport qualité : ceux de l'opération, de la série ou de l'indicateur qu'il documente. */
@ServerSidePort
public interface SimsOwnersLookup {

    List<String> ownersOf(String simsId) throws RmesException;
}
