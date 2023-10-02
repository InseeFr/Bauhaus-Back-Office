package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import org.springframework.stereotype.Service;

@Service
public class DatasetServiceImpl extends RdfService implements DatasetService {
    @Override
    public String getDatasets() throws RmesException {
        return this.repoGestion.getResponseAsArray(DatasetQueries.getDatasets()).toString();
    }

    @Override
    public String getDatasetByID(String id) throws RmesException {
        return this.repoGestion.getResponseAsObject(DatasetQueries.getDataset(id)).toString();
    }
}
