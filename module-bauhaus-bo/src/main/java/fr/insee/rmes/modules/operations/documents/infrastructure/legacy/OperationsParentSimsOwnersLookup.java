package fr.insee.rmes.modules.operations.documents.infrastructure.legacy;

import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.SimsOwnersLookup;
import java.util.List;
import org.json.JSONArray;
import org.springframework.stereotype.Component;

/**
 * Délègue au calcul historique : la cible du rapport (opération, série ou indicateur), puis les
 * créateurs de la série ou de l'indicateur.
 */
@ServerSideAdaptor
@Component
public class OperationsParentSimsOwnersLookup implements SimsOwnersLookup {

    private final OperationsParentRepository operationsParentRepository;

    public OperationsParentSimsOwnersLookup(OperationsParentRepository operationsParentRepository) {
        this.operationsParentRepository = operationsParentRepository;
    }

    @Override
    public List<String> ownersOf(String simsId) throws RmesException {
        String owners = operationsParentRepository.getDocumentationOwnersByIdSims(simsId);
        if (owners == null) {
            return List.of();
        }
        return new JSONArray(owners).toList().stream().map(String::valueOf).toList();
    }
}
