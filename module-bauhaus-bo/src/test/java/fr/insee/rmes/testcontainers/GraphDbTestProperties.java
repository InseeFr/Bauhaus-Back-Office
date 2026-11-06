package fr.insee.rmes.testcontainers;

import static fr.insee.rmes.testcontainers.WithGraphDBContainer.BAUHAUS_TEST_REPOSITORY;
import static fr.insee.rmes.testcontainers.WithGraphDBContainer.container;

import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Pointe la configuration RDF de l'application vers le conteneur GraphDB partagé.
 *
 * <p>Chaque test d'intégration recopiait ces quelques {@code registry.add} dans son
 * {@code @DynamicPropertySource} ; ils vivent ici une fois pour toutes. Le test garde la main sur
 * ce qui lui est propre : le chargement de ses fixtures {@code .trig} et ses URI de base.
 */
public final class GraphDbTestProperties {

    /** Dépôt de publication distinct du dépôt de gestion, créé à partir de {@code config-pub.ttl}. */
    public static final String PUBLICATION_REPOSITORY = "bauhaus-test-pub";

    private GraphDbTestProperties() {}

    /** Dépôt de gestion seul : suffit aux tests qui ne publient pas. */
    public static void registerGestion(DynamicPropertyRegistry registry) {
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", GraphDbTestProperties::sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
    }

    /**
     * Gestion et publication dans deux dépôts distincts, comme en production. Les partager
     * laisserait les requêtes SPARQL sans filtre de graphe ramasser les deux préfixes d'IRI.
     */
    public static void registerGestionAndDedicatedPublication(DynamicPropertyRegistry registry) {
        registerGestion(registry);
        container.withInitFolder("/testcontainers").withRepository("config-pub.ttl");
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.sesameServer", GraphDbTestProperties::sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.repository", () -> PUBLICATION_REPOSITORY);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.baseURI", () -> "http://id.insee.fr/");
    }

    /** Gestion et publication dans le même dépôt de test. */
    public static void registerGestionAndSharedPublication(DynamicPropertyRegistry registry) {
        registerGestion(registry);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.sesameServer", GraphDbTestProperties::sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.repository", () -> BAUHAUS_TEST_REPOSITORY);
    }

    private static String sesameServer() {
        return "http://" + container.getHost() + ":" + container.getMappedPort(7200);
    }
}
