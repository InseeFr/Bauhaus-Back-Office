package fr.insee.rmes.config;

import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import java.util.Optional;

/**
 * Fabrique d'IRI réelle pour les tests unitaires.
 * Toutes les URI de base relatives valent {@code baseUri}, sous la gestion {@code http://bauhaus/}.
 */
public class BauhausIriFactoryStub {

    private BauhausIriFactoryStub() {}

    public static BauhausIriFactory stub(String baseUri) {
        return new BauhausIriFactory(
                GraphsPropertiesStub.stub(),
                new BauhausUriBuilder("http://publication/", "http://bauhaus/", name -> Optional.of(baseUri)),
                DocumentationsPropertiesStub.stub());
    }
}
