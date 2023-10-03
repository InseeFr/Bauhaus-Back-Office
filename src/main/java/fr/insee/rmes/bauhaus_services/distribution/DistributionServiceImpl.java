package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import org.springframework.stereotype.Service;

@Service
public class DistributionServiceImpl extends RdfService implements DistributionService {

    @Override
    public String getDistributions() throws RmesException {
        return this.repoGestion.getResponseAsArray(DistributionQueries.getDistributions()).toString();
    }

    @Override
    public String getDistributionByID(String id) throws RmesException {
        return this.repoGestion.getResponseAsObject(DistributionQueries.getDistribution(id)).toString();
    }
}
