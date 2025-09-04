package fr.insee.rmes.onion.infrastructure.graphql.operations;

import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.model.operations.OperationFamily;
import fr.insee.rmes.onion.domain.model.operations.OperationFamilySeries;
import fr.insee.rmes.onion.domain.model.operations.OperationFamilySubject;
import fr.insee.rmes.onion.domain.model.operations.PartialOperationFamily;
import fr.insee.rmes.onion.domain.port.serverside.operations.OperationFamilyRepository;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationFamilyResolver
{
    private final OperationFamilyRepository operationFamilyRepository;


    public OperationFamilyResolver(@Qualifier("graphdb") OperationFamilyRepository operationFamilyRepository) {
        this.operationFamilyRepository = operationFamilyRepository;
    }

    @GraphQLQuery(name = "getAllFamilies")
    public List<PartialOperationFamily> getAllFamilies() throws RmesException {
        return this.operationFamilyRepository.getFamilies();
    }

    @GraphQLQuery(name = "getFamily")
    public OperationFamily getFamily(
            @GraphQLNonNull
            @GraphQLArgument(name = "id") String id
    ) throws RmesException {
        return this.operationFamilyRepository.getFamily(id);
    }

    @GraphQLQuery(name = "series")
    public List<OperationFamilySeries> series(
            @GraphQLContext OperationFamily family
    ) throws RmesException {
        return operationFamilyRepository.getFamilySeries(family.id());
    }

    @GraphQLQuery(name = "subjects")
    public List<OperationFamilySubject> subjects(
            @GraphQLContext OperationFamily family
    ) throws RmesException {
        return operationFamilyRepository.getFamilySubjects(family.id());
    }

}
