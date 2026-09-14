package fr.insee.rmes.modules.ddi.operations_mirror.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.operation.domain.event.OperationSaved;
import fr.insee.rmes.modules.operation.domain.event.SeriesSaved;

/**
 * Tient le dépôt DDI en miroir des séries et opérations statistiques du dépôt RDF : une série y
 * devient un {@code Group}, une opération une {@code StudyUnit}.
 * <p>
 * C'est la couche d'anti-corruption entre les deux dépôts : le domaine « opérations » se contente
 * d'annoncer ce qu'il a écrit, et toute la connaissance de la correspondance RDF ↔ DDI vit ici.
 */
@ClientSidePort
public interface OperationsMirrorService {

    void mirror(SeriesSaved event);

    void mirror(OperationSaved event);
}
