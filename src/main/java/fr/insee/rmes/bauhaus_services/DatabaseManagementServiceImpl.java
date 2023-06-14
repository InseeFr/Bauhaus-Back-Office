package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseManagementServiceImpl extends RdfService implements DatabaseManagementService {

    @Override
    public void clearGraph() throws RmesBadRequestException {
        if("prod".equalsIgnoreCase(config.getEnv())){
            throw new RmesBadRequestException("This action is not allowed on this environnement.");
        }
        this.repoGestion.clearGraph();
    }
}
