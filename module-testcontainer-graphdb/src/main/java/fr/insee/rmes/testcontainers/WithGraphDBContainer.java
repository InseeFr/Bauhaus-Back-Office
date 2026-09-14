package fr.insee.rmes.testcontainers;

import fr.insee.rmes.graphdb.RdfConnectionDetails;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base des tests d'intégration qui ont besoin d'un triplestore.
 *
 * <p>Le conteneur est un <em>singleton</em> : il est démarré une seule fois pour toute la JVM de
 * tests, puis réclamé par Ryuk à l'arrêt de celle-ci. L'annotation {@code @Testcontainers} n'est
 * volontairement pas utilisée : elle attache le cycle de vie du conteneur à la classe de tests, ce
 * qui redémarrait GraphDB pour chacune des dizaines de classes qui héritent d'ici — soit ~20 s
 * perdues à chaque fois, GraphDB n'acceptant de répondre qu'après une quinzaine de secondes.
 *
 * <p>L'isolation entre classes est assurée à la place par {@link GraphDBResetExtension}, qui vide
 * les dépôts avant chaque classe : chacune retrouve donc la base vierge qu'un redémarrage lui
 * donnait, sans en payer le prix.
 */
@ExtendWith(GraphDBResetExtension.class)
public class WithGraphDBContainer {

    /**
     * Image du triplestore utilisée par tous les tests d'intégration. La ligne est annotée pour que
     * Renovate (custom manager « docker images in Java sources », voir renovate.json) propose la mise
     * à jour du tag comme pour n'importe quelle autre dépendance. Garder l'annotation juste au-dessus
     * de la constante et le tag dans le littéral, sans concaténation.
     */
    // renovate: datasource=docker depName=ontotext/graphdb
    public static final String GRAPHDB_IMAGE = "ontotext/graphdb:10.8.4";

    public static final GraphDBContainer container = startSharedContainer();
    public static final String BAUHAUS_TEST_REPOSITORY = GraphDBContainer.GESTION_REPOSITORY;

    private static GraphDBContainer startSharedContainer() {
        GraphDBContainer sharedContainer = new GraphDBContainer(GRAPHDB_IMAGE);
        sharedContainer.start();
        return sharedContainer;
    }

    protected static RdfConnectionDetails getRdfGestionConnectionDetails() {
        return new RdfConnectionDetails() {
            @Override
            public String getUrlServer() {
                return "http://" + container.getHost() + ":" + container.getMappedPort(GraphDBContainer.GRAPHDB_PORT);
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
