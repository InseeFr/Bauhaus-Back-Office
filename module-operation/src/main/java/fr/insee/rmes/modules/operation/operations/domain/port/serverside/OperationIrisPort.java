package fr.insee.rmes.modules.operation.operations.domain.port.serverside;

import java.util.List;

public interface OperationIrisPort {

    /**
     * Les IRI sous lesquelles un autre référentiel peut désigner l'opération {@code operationId} :
     * celle de publication puis celle de gestion.
     */
    List<String> irisOf(String operationId);
}
