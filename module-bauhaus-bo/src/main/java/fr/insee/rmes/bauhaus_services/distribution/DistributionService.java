package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.datasets.distributions.model.Distribution;
import fr.insee.rmes.modules.datasets.distributions.model.DistributionsForSearch;
import fr.insee.rmes.modules.datasets.distributions.model.PartialDistribution;
import fr.insee.rmes.modules.datasets.distributions.model.PatchDistribution;

import java.util.List;

public interface DistributionService {

    List<PartialDistribution> getDistributions() throws RmesException;

    List<DistributionsForSearch> getDistributionsForSearch() throws RmesException;

    Distribution getDistributionByID(String id) throws RmesException;

    String create(String body) throws RmesException;

    String update(String id, String body) throws RmesException;

    String publishDistribution(String id) throws RmesException;

    void patchDistribution(String distributionId, PatchDistribution distribution) throws RmesException;

    void deleteDistributionId(String distributionId) throws RmesException;
}