package fr.insee.rmes.testcontainers;

import fr.insee.rmes.graphdb.RdfConnectionDetails;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
public class WithGraphDBContainer {

    /**
     * Image du triplestore utilisée par tous les tests d'intégration. La ligne est annotée pour que
     * Renovate (custom manager « docker images in Java sources », voir renovate.json) propose la mise
     * à jour du tag comme pour n'importe quelle autre dépendance. Garder l'annotation juste au-dessus
     * de la constante et le tag dans le littéral, sans concaténation.
     */
    // renovate: datasource=docker depName=ontotext/graphdb
    public static final String GRAPHDB_IMAGE = "ontotext/graphdb:10.8.4";

    @Container
    public static final GraphDBContainer container = new GraphDBContainer(GRAPHDB_IMAGE);
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
