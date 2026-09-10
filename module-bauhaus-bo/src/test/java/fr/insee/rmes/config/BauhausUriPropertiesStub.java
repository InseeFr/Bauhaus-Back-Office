package fr.insee.rmes.config;

import fr.insee.rmes.BauhausUriProperties;

public class BauhausUriPropertiesStub {

    private BauhausUriPropertiesStub() {}

    public static BauhausUriProperties stub() {
        return new BauhausUriProperties(
                "http://bauhaus/", "http://links/", "http://codelist/", "http://documents/", "produits/indicateur");
    }
}
