package fr.insee.rmes.testcontainers;

import fr.insee.rmes.graphdb.RdfConnectionDetails;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base des tests d'intégration qui tournent sur Fuseki, pendant de {@code WithGraphDBContainer}.
 *
 * <p>Le conteneur est un singleton démarré une fois pour toute la JVM de tests ; l'isolation entre
 * classes vient de {@link FusekiResetExtension}, qui vide le dataset avant chacune.
 */
@ExtendWith(FusekiResetExtension.class)
public class WithFusekiContainer {

    /**
     * Image du triplestore. Garder l'annotation Renovate juste au-dessus de la constante et le tag dans
     * le littéral, sans concaténation (voir {@code WithGraphDBContainer}).
     */
    // renovate: datasource=docker depName=stain/jena-fuseki
    public static final String FUSEKI_IMAGE = "stain/jena-fuseki:5.1.0";

    public static final FusekiContainer container = startSharedContainer();

    private static FusekiContainer startSharedContainer() {
        FusekiContainer sharedContainer = new FusekiContainer(FUSEKI_IMAGE);
        sharedContainer.start();
        return sharedContainer;
    }

    protected static RdfConnectionDetails getRdfGestionConnectionDetails() {
        return new RdfConnectionDetails() {
            @Override
            public String getUrlServer() {
                return "http://" + container.getHost() + ":" + container.getMappedPort(FusekiContainer.FUSEKI_PORT);
            }

            @Override
            public String repositoryId() {
                return FusekiContainer.DATASET;
            }
        };
    }

    /** Le chargeur de fixtures neutre vis-à-vis du triplestore, comme {@code WithGraphDBContainer.fixtureLoader()}. */
    protected static SparqlFixtureLoader fixtureLoader() {
        return container;
    }
}
