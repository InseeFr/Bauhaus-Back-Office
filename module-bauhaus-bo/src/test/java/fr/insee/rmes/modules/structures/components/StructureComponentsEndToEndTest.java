package fr.insee.rmes.modules.structures.components;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parcours complet d'un composant mutualisé de structure, du POST à la relecture, contre un vrai
 * triplestore.
 *
 * <p>Ce que ce test verrouille et qu'aucun test de contrôleur ne voit : les triplets réellement
 * écrits par {@code StructureComponentRepository} — l'IRI dérivée de l'URI de base et du type, le
 * graphe de destination, les facettes {@code xsd:} d'un composant typé, la classe
 * {@code qb:CodedProperty} d'un composant adossé à une liste de codes, et le repli en littéral
 * d'une valeur d'attribut libre qui n'est pas une IRI.
 *
 * <p>Les URI de base sont fixées ici plutôt que laissées à la configuration ambiante : c'est ce qui
 * permet d'affirmer l'IRI exacte du composant créé.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://bauhaus/")
class StructureComponentsEndToEndTest extends WithGraphDBContainer {

    private static final String COMPONENTS_GRAPH = "http://rdf.insee.fr/graphes/composants";
    private static final String COMPONENTS_BASE_URI = "http://bauhaus/structuresDeDonnees/composants/";

    private static final String QB = "http://purl.org/linked-data/cube#";
    private static final String XSD = "http://www.w3.org/2001/XMLSchema#";
    private static final String INSEE = "http://rdf.insee.fr/def/base#";
    private static final String SKOS_CONCEPT = "http://www.w3.org/2004/02/skos/core#Concept";

    @LocalServerPort
    int serverPort;

    @Autowired
    RepositoryGestion repositoryGestion;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer",
                () -> "http://" + container.getHost() + ":" + container.getMappedPort(7200));
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
    }

    @Test
    void a_created_dimension_is_readable_and_stored_at_its_expected_iri() throws RmesException {
        String id = create("""
                {
                  "identifiant": "NOTATION_DIM",
                  "labelLg1": "Dimension géographique",
                  "labelLg2": "Geographic dimension",
                  "altLabelLg1": "Géo",
                  "descriptionLg1": "Une description",
                  "type": "%sDimensionProperty",
                  "creator": "http://bauhaus/HIE000000",
                  "contributor": ["http://bauhaus/HIE000000"],
                  "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Public"
                }""".formatted(QB));

        assertThat(id).startsWith("d");

        JSONObject component = new JSONObject(read(id));
        assertThat(component.getString("id")).isEqualTo(id);
        assertThat(component.getString("identifiant")).isEqualTo("NOTATION_DIM");
        assertThat(component.getString("labelLg1")).isEqualTo("Dimension géographique");
        assertThat(component.getString("labelLg2")).isEqualTo("Geographic dimension");
        assertThat(component.getString("altLabelLg1")).isEqualTo("Géo");
        assertThat(component.getString("descriptionLg1")).isEqualTo("Une description");
        assertThat(component.getString("validationState")).isEqualTo("Unpublished");
        assertThat(component.getString("creator")).isEqualTo("http://bauhaus/HIE000000");

        String iri = COMPONENTS_BASE_URI + "dimension/" + id;
        assertThat(triplePresent(iri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "<" + QB + "DimensionProperty>"))
                .as("le composant est typé qb:DimensionProperty dans le graphe des composants").isTrue();
        assertThat(triplePresent(iri, "http://purl.org/dc/terms/identifier", "\"" + id + "\""))
                .as("dcterms:identifier est un littéral simple").isTrue();
        assertThat(triplePresent(iri, "http://www.w3.org/2000/01/rdf-schema#label", "\"Dimension géographique\"@fr"))
                .as("le libellé porte la langue lg1").isTrue();
        assertThat(triplePresent(iri, INSEE + "validationState", "\"Unpublished\""))
                .isTrue();
    }

    @Test
    void a_string_ranged_measure_carries_its_xsd_facets() throws RmesException {
        String id = create("""
                {
                  "identifiant": "NOTATION_MES",
                  "labelLg1": "Mesure texte",
                  "labelLg2": "Text measure",
                  "type": "%sMeasureProperty",
                  "creator": "http://bauhaus/HIE000000",
                  "contributor": ["http://bauhaus/HIE000000"],
                  "range": "%sstring",
                  "minLength": "1",
                  "maxLength": "10",
                  "pattern": "[A-Z]+"
                }""".formatted(QB, XSD));

        String iri = COMPONENTS_BASE_URI + "mesure/" + id;
        assertThat(triplePresent(iri, "http://www.w3.org/2000/01/rdf-schema#range", "<" + XSD + "string>")).isTrue();
        assertThat(triplePresent(iri, XSD + "minLength", "\"1\"@fr")).isTrue();
        assertThat(triplePresent(iri, XSD + "maxLength", "\"10\"@fr")).isTrue();
        assertThat(triplePresent(iri, XSD + "pattern", "\"[A-Z]+\"@fr")).isTrue();
    }

    @Test
    void a_code_list_ranged_attribute_is_a_coded_property() throws RmesException {
        String id = create("""
                {
                  "identifiant": "NOTATION_ATT",
                  "labelLg1": "Attribut codé",
                  "labelLg2": "Coded attribute",
                  "type": "%sAttributeProperty",
                  "creator": "http://bauhaus/HIE000000",
                  "contributor": ["http://bauhaus/HIE000000"],
                  "range": "%scodeList",
                  "codeList": "http://bauhaus/codes/listeCodes",
                  "fullCodeListValue": "http://bauhaus/codes/listeCodes"
                }""".formatted(QB, INSEE));

        String iri = COMPONENTS_BASE_URI + "attribut/" + id;
        assertThat(triplePresent(iri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "<" + QB + "CodedProperty>"))
                .as("un composant adossé à une liste de codes est aussi une qb:CodedProperty").isTrue();
        assertThat(triplePresent(iri, "http://www.w3.org/2000/01/rdf-schema#range", "<" + SKOS_CONCEPT + ">"))
                .as("faute de classe OWL déclarée, la portée retombe sur skos:Concept").isTrue();
        assertThat(triplePresent(iri, QB + "codeList", "<http://bauhaus/codes/listeCodes>")).isTrue();
    }

    /**
     * Les attributs libres arrivent en paires {@code attribute_N} / {@code attributeValue_N}. Une
     * valeur qui est une IRI devient une ressource ; une valeur quelconque doit malgré tout être
     * enregistrée, en littéral, plutôt que faire échouer l'écriture.
     */
    @Test
    void a_free_attribute_value_is_stored_as_an_iri_or_falls_back_to_a_literal() throws RmesException {
        String id = create("""
                {
                  "identifiant": "NOTATION_LIBRE",
                  "labelLg1": "Dimension à attributs",
                  "labelLg2": "Dimension with attributes",
                  "type": "%sDimensionProperty",
                  "creator": "http://bauhaus/HIE000000",
                  "contributor": ["http://bauhaus/HIE000000"],
                  "attribute_0": "http://bauhaus/attribut/lien",
                  "attributeValue_0": "http://bauhaus/valeur/cible",
                  "attribute_1": "http://bauhaus/attribut/texte",
                  "attributeValue_1": "une valeur libre"
                }""".formatted(QB));

        String iri = COMPONENTS_BASE_URI + "dimension/" + id;
        assertThat(triplePresent(iri, "http://bauhaus/attribut/lien", "<http://bauhaus/valeur/cible>"))
                .as("une valeur d'attribut qui est une IRI est stockée en ressource").isTrue();
        assertThat(triplePresent(iri, "http://bauhaus/attribut/texte", "\"une valeur libre\""))
                .as("une valeur d'attribut qui n'est pas une IRI retombe en littéral").isTrue();
    }

    @Test
    void a_deleted_component_is_gone_from_the_triplestore() throws RmesException {
        String id = create("""
                {
                  "identifiant": "NOTATION_SUPPR",
                  "labelLg1": "À supprimer",
                  "labelLg2": "To be deleted",
                  "type": "%sDimensionProperty",
                  "creator": "http://bauhaus/HIE000000",
                  "contributor": ["http://bauhaus/HIE000000"]
                }""".formatted(QB));

        String iri = COMPONENTS_BASE_URI + "dimension/" + id;
        assertThat(componentExists(iri)).isTrue();

        RestClient.create().delete()
                .uri(componentsEndpoint() + "/" + id)
                .retrieve()
                .toBodilessEntity();

        assertThat(componentExists(iri)).as("le composant a disparu du graphe").isFalse();
    }

    private String componentsEndpoint() {
        return "http://localhost:" + serverPort + "/api/structures/components";
    }

    private String create(String body) {
        var response = RestClient.create().post()
                .uri(componentsEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private String read(String id) {
        return RestClient.create().get()
                .uri(componentsEndpoint() + "/" + id)
                .retrieve()
                .body(String.class);
    }

    private boolean triplePresent(String subject, String predicate, String object) throws RmesException {
        return repositoryGestion.getResponseAsBoolean(
                "ASK { GRAPH <%s> { <%s> <%s> %s } }".formatted(COMPONENTS_GRAPH, subject, predicate, object));
    }

    private boolean componentExists(String subject) throws RmesException {
        return repositoryGestion.getResponseAsBoolean(
                "ASK { GRAPH <%s> { <%s> ?p ?o } }".formatted(COMPONENTS_GRAPH, subject));
    }
}
