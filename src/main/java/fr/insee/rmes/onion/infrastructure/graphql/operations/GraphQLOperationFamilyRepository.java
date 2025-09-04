package fr.insee.rmes.onion.infrastructure.graphql.operations;

import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.model.operations.OperationFamily;
import fr.insee.rmes.onion.domain.model.operations.OperationFamilySeries;
import fr.insee.rmes.onion.domain.model.operations.OperationFamilySubject;
import fr.insee.rmes.onion.domain.model.operations.PartialOperationFamily;
import fr.insee.rmes.onion.domain.port.serverside.operations.OperationFamilyRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Qualifier("graphql")
@Repository
public class GraphQLOperationFamilyRepository implements OperationFamilyRepository {
    private final HttpGraphQlClient client;

    public GraphQLOperationFamilyRepository(HttpGraphQlClient client) {
        this.client = client;
    }

    @Override
    public List<PartialOperationFamily> getFamilies() throws RmesException {
        String query =
                """
                    {
                        getAllFamilies {
                            id
                            label
                        }
                    }
                """;

        return client.document(query).retrieveSync("getAllFamilies").toEntityList(PartialOperationFamily.class);
    }

    @Override
    public OperationFamily getFullFamily(String id) {
        String query =
                """
                    query GetFullFamily($id: String!) {
                        getFamily(id: $id) {
                            abstractLg1
                            id
                            prefLabelLg1
                            prefLabelLg2
                            series {
                                id
                                labelLg1
                                labelLg2
                            }
                            subjects {
                                id
                                labelLg1
                                labelLg2
                            }
                            validationState
                            abstractLg2
                            created
                            modified
                        }
                    }
                """;

        return client.document(query).variable("id", id).retrieveSync("getFamily").toEntity(OperationFamily.class);
    }

    @Override
    public OperationFamily getFamily(String id) {
        String query =
                """
                    query GetFullFamily($id: String!) {
                        getFamily(id: $id) {
                            abstractLg1
                            id
                            prefLabelLg1
                            prefLabelLg2
                            validationState
                            abstractLg2
                            created
                            modified
                        }
                    }
                """;

        return client.document(query).variable("id", id).retrieveSync("getFamily").toEntity(OperationFamily.class);
    }

    @Override
    public List<OperationFamilySeries> getFamilySeries(String id) {
        String query =
                """
                    query GetFullFamily($id: String!) {
                        getFamily(id: $id) {
                            series {
                                id
                                labelLg1
                                labelLg2
                            }
                        }
                    }
                """;

        return client.document(query).variable("id", id).retrieveSync("getFamily").toEntityList(OperationFamilySeries.class);
    }

    @Override
    public List<OperationFamilySubject> getFamilySubjects(String id) {
        String query =
                """
                    query GetFullFamily($id: String!) {
                        getFamily(id: $id) {
                            subjects {
                                id
                                labelLg1
                                labelLg2
                            }
                        }
                    }
                """;

        return client.document(query).variable("id", id).retrieveSync("getFamily").toEntityList(OperationFamilySubject.class);
    }

}
