package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GraphDBCollectionPropertiesTest {

    private static final String GRAPH = "http://rdf.insee.fr/graphes/concepts/definitions";

    @Test
    @DisplayName("l'IRI d'une collection est celle de la base URI suffixée par l'identifiant")
    void builds_the_resource_iri_from_the_base_uri_and_the_id() {
        var properties = new GraphDBCollectionProperties(GRAPH, "http://bauhaus/concepts/definitions");

        assertThat(properties.getResourceIRI("c1000").stringValue())
                .isEqualTo("http://bauhaus/concepts/definitions/c1000");
    }

    @Test
    @DisplayName("une base URI de gestion terminée par '/' ne doit pas produire une IRI à double slash")
    void collapses_the_slash_duplicated_by_a_trailing_slash_base_uri() {
        // `collections.baseURI` vaut `${...sesame.gestion.baseURI}/concepts/definitions` : quand
        // la base URI de gestion se termine par `/`, la concaténation double le séparateur et
        // crée une seconde ressource, distincte de la collection historique.
        var properties = new GraphDBCollectionProperties(GRAPH, "http://bauhaus//concepts/definitions");

        assertThat(properties.getResourceIRI("c1000").stringValue())
                .isEqualTo("http://bauhaus/concepts/definitions/c1000");
    }

    @Test
    @DisplayName("le séparateur du schéma est préservé")
    void keeps_the_scheme_separator() {
        var properties = new GraphDBCollectionProperties(GRAPH, "https://bauhaus/concepts/definitions");

        assertThat(properties.getResourceIRI("qq").stringValue()).isEqualTo("https://bauhaus/concepts/definitions/qq");
    }
}
