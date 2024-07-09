package fr.insee.rmes.testcontainers.queries;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfConnectionDetails;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;


@Testcontainers
public class WithGraphDBContainer {
    @Container
    static GraphDBContainer container = new GraphDBContainer("ontotext/graphdb:10.6.4");

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        container.withInitFolder("/testcontainers").withRepository("config.ttl");
    }

    protected static RdfConnectionDetails getRdfGestionConnectionDetails() {
        return new RdfConnectionDetails() {
            @Override
            public String getUrlServer() {
                return "http://" + container.getHost() + ":" + container.getMappedPort(7200);
            }

            @Override
            public String repositoryId() {
                return "bauhaus-test";
            }
        };
    }

}
