package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.PatchDataset;

public interface DatasetService {

    String getDatasets() throws RmesException;

    String getDatasetByID(String id) throws RmesException ;

    String update(String datasetId, String body) throws RmesException;

    String create(String body) throws RmesException;

    String getDistributions(String id) throws RmesException;

    String getArchivageUnits() throws RmesException;

    void patchDataset(String datasetId, PatchDataset patchDataset) throws RmesException;

    String getDatasetsForDistributionCreation(String stamp) throws RmesException;

    String publishDataset(String id) throws RmesException;
}