package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.datasets.datasets.model.Dataset;
import fr.insee.rmes.modules.datasets.datasets.model.DatasetsForSearch;
import fr.insee.rmes.modules.datasets.datasets.model.PartialDataset;
import fr.insee.rmes.modules.datasets.datasets.model.PatchDataset;

import java.util.List;
import java.util.Set;

public interface DatasetService {

    List<PartialDataset> getDatasets() throws RmesException;

    List<DatasetsForSearch> getDatasetsForSearch() throws RmesException;

    Dataset getDatasetByID(String id) throws RmesException ;

    String update(String datasetId, String body) throws RmesException;

    String create(String body) throws RmesException;

    String getDistributions(String id) throws RmesException;

    String getArchivageUnits() throws RmesException;

    void patchDataset(String datasetId, PatchDataset patchDataset) throws RmesException;

    List<PartialDataset> getDatasetsForDistributionCreation(Set<String> stamp) throws RmesException;

    String publishDataset(String id) throws RmesException;

    void deleteDatasetId(String datasetId) throws RmesException;

}