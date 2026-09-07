package fr.insee.rmes.config;

import fr.insee.rmes.GraphsProperties;

public class GraphsPropertiesStub {

    private static final String OPERATIONS_GRAPH_SUFFIX = "operations";
    private static final String STRUCTURES_COMPONENTS_GRAPH_SUFFIX = "composants";

    private GraphsPropertiesStub() {
    }

    public static GraphsProperties stub() {
        return stub(OPERATIONS_GRAPH_SUFFIX, STRUCTURES_COMPONENTS_GRAPH_SUFFIX);
    }

    /**
     * Variante du stub pour les tests d'intégration : le conteneur GraphDB est partagé entre toutes
     * les classes de test, donc une requête qui balaie un graphe entier (le dernier identifiant, par
     * exemple) voit aussi les fixtures des autres classes. Isoler le graphe rend le test déterministe.
     */
    public static GraphsProperties stub(String operationsGraphSuffix, String structuresComponentsGraphSuffix) {
        return new GraphsProperties(
                "http://rdf.insee.fr/graphes/",
                "concepts/",
                "codes/nomenclatures",
                operationsGraphSuffix,
                "qualite/rapport",
                "qualite/simsv2fr",
                "concepts/qualite",
                "qualite/territoires",
                "qualite/documents",
                "produits",
                "structures",
                structuresComponentsGraphSuffix,
                "codes",
                "organisations",
                "organisations/insee",
                "geo/cog");
    }
}
