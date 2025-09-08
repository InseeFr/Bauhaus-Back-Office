package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.model.dataset.DatasetsForSearch;
import fr.insee.rmes.model.dataset.PartialDataset;
import fr.insee.rmes.model.dataset.PatchDataset;

import java.util.List;

public interface DatasetService {

    List<PartialDataset> getDatasets() throws RmesException;

    List<DatasetsForSearch> getDatasetsForSearch() throws RmesException;

    Dataset getDatasetByID(String id) throws RmesException ;

    String update(String datasetId, String body) throws RmesException;

    String create(String body) throws RmesException;

    String getDistributions(String id) throws RmesException;

    String getArchivageUnits() throws RmesException;

    void patchDataset(String datasetId, PatchDataset patchDataset) throws RmesException;

    List<PartialDataset> getDatasetsForDistributionCreation(String stamp) throws RmesException;

    String publishDataset(String id) throws RmesException;

    void deleteDatasetId(String datasetId) throws RmesException;

}