package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.exceptions.RmesException;

public interface DatasetService {

    String getDatasets() throws RmesException;

    String getDatasetByID(String id) throws RmesException ;

    String update(String datasetId, String body) throws RmesException;

    String create(String body) throws RmesException;

    String getDistributions(String id) throws RmesException;

    String getArchivageUnits() throws RmesException;
}
