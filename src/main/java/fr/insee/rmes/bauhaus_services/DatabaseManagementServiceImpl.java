package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DatabaseManagementServiceImpl extends RdfService implements DatabaseManagementService {

    @Value("classpath:data/dataset.trig")
    private Resource res;

    Resource getRes(){
        return this.res;
    }

    @Override
    public void clearGraph() throws RmesBadRequestException, IOException {
        if("prod".equalsIgnoreCase(config.getEnv())){
            throw new RmesBadRequestException("This action is not allowed on this environnement.");
        }
        this.repoGestion.clearGraph(this.getRes().getFile());
    }
}
