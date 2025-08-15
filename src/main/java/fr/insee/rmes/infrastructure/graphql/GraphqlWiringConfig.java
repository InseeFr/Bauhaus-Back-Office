package fr.insee.rmes.infrastructure.graphql;

import graphql.schema.GraphQLObjectType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphqlWiringConfig {

    @Bean
    RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .type("AllStructure", typeWiring -> typeWiring.typeResolver(env -> {
                    Object obj = env.getObject();
                    if (obj instanceof Structure) {
                        return env.getSchema().getObjectType("Structure");
                    }
                    if (obj instanceof DataRelationshipRecord) {
                        return env.getSchema().getObjectType("DataRelationShip");
                    }
                    // Si rien ne correspond, GraphQL lèvera une erreur
                    return (GraphQLObjectType) null;
                }));
    }
}