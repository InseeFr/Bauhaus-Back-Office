package fr.insee.rmes.config;

import fr.insee.rmes.GraphsProperties;

public class GraphsPropertiesStub {

    private GraphsPropertiesStub() {
    }

    public static GraphsProperties stub() {
        return new GraphsProperties(
                "http://rdf.insee.fr/graphes/",
                "concepts/",
                "codes/nomenclatures",
                "operations",
                "qualite/rapport",
                "qualite/simsv2fr",
                "concepts/qualite",
                "qualite/territoires",
                "qualite/documents",
                "produits",
                "structures",
                "composants",
                "codes",
                "organisations",
                "organisations/insee",
                "geo/cog");
    }
}
