package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.PatchDistribution;

public interface DistributionService {

    String getDistributions() throws RmesException;

    String getDistributionByID(String id) throws RmesException;

    String create(String body) throws RmesException;

    String update(String id, String body) throws RmesException;

    String publishDistribution(String id) throws RmesException;

    void PatchDistribution(String distributionId, PatchDistribution distribution) throws RmesException;
}