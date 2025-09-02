package fr.insee.rmes.onion.infrastructure.graphdb.operations;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.model.operations.PartialOperationFamily;
import fr.insee.rmes.onion.domain.port.serverside.operations.OperationFamilyRepository;
import fr.insee.rmes.onion.infrastructure.graphdb.operations.queries.OperationFamilyQueries;
import fr.insee.rmes.utils.DiacriticSorter;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GraphDBOperationFamilyRepository implements OperationFamilyRepository {

    private final RepositoryGestion repositoryGestion;
    private final OperationFamilyQueries operationFamilyQueries;

    public GraphDBOperationFamilyRepository(RepositoryGestion repositoryGestion, OperationFamilyQueries operationFamilyQueries) {
        this.repositoryGestion = repositoryGestion;
        this.operationFamilyQueries = operationFamilyQueries;
    }

    @Override
    public List<PartialOperationFamily> getFamilies() throws RmesException {
        var families = this.repositoryGestion.getResponseAsArray(operationFamilyQueries.familiesQuery());

        return DiacriticSorter.sort(families,
                PartialOperationFamily[].class,
                PartialOperationFamily::label);
    }
}
