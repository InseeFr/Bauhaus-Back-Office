package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.exceptions.RmesException;

public interface DistributionService {

    String getDistributions() throws RmesException;

    String getDistributionByID(String id) throws RmesException;    
}
