package fr.insee.rmes.testcontainers.queries;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfConnectionDetails;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;



@Testcontainers
public class WithGraphDBContainer {
    @Container
    public static GraphDBContainer container = new GraphDBContainer("ontotext/graphdb:10.8.4");


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
