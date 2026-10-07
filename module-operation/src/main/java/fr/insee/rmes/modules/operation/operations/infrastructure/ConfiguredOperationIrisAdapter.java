package fr.insee.rmes.modules.operation.operations.infrastructure;

import fr.insee.rmes.PropertiesKeys;
import fr.insee.rmes.modules.operation.operations.domain.port.serverside.OperationIrisPort;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * IRI d'une opération dérivées de la configuration. Les StudyUnits DDI désignent leur opération
 * sous l'une ou l'autre : l'init DDI écrit celle de publication, la création depuis l'IHM celle de
 * gestion.
 */
@Component
public class ConfiguredOperationIrisAdapter implements OperationIrisPort {

    private final String publicationBaseUri;
    private final String gestionBaseUri;
    private final String operationsBaseUri;

    public ConfiguredOperationIrisAdapter(
            @Value("${" + PropertiesKeys.BASE_URI_PUBLICATION + "}") String publicationBaseUri,
            @Value("${" + PropertiesKeys.BASE_URI_GESTION + "}") String gestionBaseUri,
            @Value("${" + PropertiesKeys.OPERATIONS_BASE_URI + "}") String operationsBaseUri) {
        this.publicationBaseUri = publicationBaseUri;
        this.gestionBaseUri = gestionBaseUri;
        this.operationsBaseUri = operationsBaseUri;
    }

    @Override
    public List<String> irisOf(String operationId) {
        String path = operationsBaseUri + "/" + operationId;
        return List.of(publicationBaseUri + path, gestionBaseUri + path);
    }
}
