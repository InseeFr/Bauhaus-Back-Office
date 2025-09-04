package fr.insee.rmes.onion.infrastructure.graphql.webservice;

import fr.insee.rmes.onion.infrastructure.graphql.operations.OperationFamilyResolver;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GraphQLResources {

    private final GraphQL graphQL;

    public GraphQLResources(OperationFamilyResolver operationFamilyResolver) {

        GraphQLSchema schema = new GraphQLSchemaGenerator()
                .withBasePackages("fr.insee.rmes.onion.infrastructure.graphql")
                .withBasePackages("fr.insee.rmes.onion.domain.model")
                .withOperationsFromSingleton(operationFamilyResolver)
                .generate();

        this.graphQL = new GraphQL.Builder(schema).build();
    }

    @PostMapping(value = "/graphql")
    public Map<String, Object> execute(@RequestBody Map<String, Object> request){
        String query = (String) request.get("query");
        String operationName = (String) request.get("operationName");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.getOrDefault("variables", Map.of());

        ExecutionInput input = ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variables)
                .build();

        ExecutionResult result = graphQL.execute(input);

        return result.toSpecification();
    }

}
