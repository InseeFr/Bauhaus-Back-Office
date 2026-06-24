package fr.insee.rmes.testcontainers;

import fr.insee.rmes.graphdb.RdfConnectionDetails;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
public class WithGraphDBContainer {

    @Container
    public static final GraphDBContainer container = new GraphDBContainer("ontotext/graphdb:10.8.4");
    public static final String BAUHAUS_TEST_REPOSITORY = "bauhaus-test";


    protected static RdfConnectionDetails getRdfGestionConnectionDetails() {
        return new RdfConnectionDetails() {
            @Override
            public String getUrlServer() {
                return "http://" + container.getHost() + ":" + container.getMappedPort(7200);
            }

            @Override
            public String repositoryId() {
                return BAUHAUS_TEST_REPOSITORY;
            }
        };
    }

    /**
     * The backend-neutral fixture loader for the running container. Tests that only need to
     * push {@code .trig} fixtures should depend on this seam rather than on {@link GraphDBContainer}
     * directly, so they remain unchanged once a {@code FusekiContainer} provides the same contract.
     */
    protected static SparqlFixtureLoader fixtureLoader() {
        return container;
    }

}
