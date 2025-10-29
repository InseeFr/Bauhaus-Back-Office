package fr.insee.rmes.testcontainers;

import fr.insee.rmes.graphdb.RdfConnectionDetails;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
public class WithGraphDBContainer {

    @Container
    public static final GraphDBContainer container = new GraphDBContainer("ontotext/graphdb:10.8.4");


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