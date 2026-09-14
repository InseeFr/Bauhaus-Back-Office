package fr.insee.rmes.bauhaus_services.rdf_utils;

import static fr.insee.rmes.PropertiesKeys.CONCEPTS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.STRUCTURES_COMPONENTS_BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.config.GraphsPropertiesStub;
import java.util.Optional;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

/**
 * Contrat de {@link BauhausIriFactory}, successeur injectable de la partie « à état » de
 * {@code RdfUtils} : les IRI et les graphes qui dépendent de la configuration.
 */
class BauhausIriFactoryTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

    private final BauhausIriFactory iriFactory = new BauhausIriFactory(
            GraphsPropertiesStub.stub(),
            new BauhausUriBuilder("http://publication/", "http://bauhaus/", name -> switch (name) {
                case STRUCTURES_COMPONENTS_BASE_URI -> Optional.of("composants/");
                case CONCEPTS_BASE_URI -> Optional.of("concepts/definition");
                default -> Optional.empty();
            }));

    @Test
    void structureComponentGraph_is_the_configured_components_graph() {
        assertThat(iriFactory.structureComponentGraph())
                .isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/composants"));
    }

    @Test
    void attribute_iri_is_built_from_the_components_base_uri() {
        assertThat(iriFactory.structureComponentAttribute("a1000"))
                .isEqualTo(VF.createIRI("http://bauhaus/composants/attribut/a1000"));
    }

    @Test
    void dimension_iri_is_built_from_the_components_base_uri() {
        assertThat(iriFactory.structureComponentDimension("d1000"))
                .isEqualTo(VF.createIRI("http://bauhaus/composants/dimension/d1000"));
    }

    @Test
    void measure_iri_is_built_from_the_components_base_uri() {
        assertThat(iriFactory.structureComponentMeasure("m1000"))
                .isEqualTo(VF.createIRI("http://bauhaus/composants/mesure/m1000"));
    }

    /**
     * Un identifiant déjà absolu est une IRI complète : elle est reprise telle quelle, sans être
     * préfixée une seconde fois par l'URI de base.
     */
    @Test
    void an_absolute_identifier_is_kept_as_is() {
        assertThat(iriFactory.structureComponentDimension("http://autre/composant/d1000"))
                .isEqualTo(VF.createIRI("http://autre/composant/d1000"));
    }

    @Test
    void conceptBaseUri_is_the_gestion_base_uri_of_concepts() {
        assertThat(iriFactory.conceptBaseUri()).isEqualTo("http://bauhaus/concepts/definition");
    }
}
