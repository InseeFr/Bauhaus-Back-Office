package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.exceptions.RmesException;

public interface DatasetService {

    String getDatasets() throws RmesException;

    String getDatasetByID(String id) throws RmesException ;
}
