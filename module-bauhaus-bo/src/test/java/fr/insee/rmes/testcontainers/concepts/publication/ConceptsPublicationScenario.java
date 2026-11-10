package fr.insee.rmes.testcontainers.concepts.publication;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.DocumentationsProperties;
import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb.GraphDBCollectionProperties;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Publication d'un concept et d'une collection, rejouée sur chaque triplestore (voir
 * {@link ConceptsPublicationGraphDBIntegrationTest} et {@link ConceptsPublicationFusekiIntegrationTest}).
 *
 * <p>Gestion et publication partagent le même dépôt : l'objet publié se distingue par son IRI,
 * réécrite dans la base de publication. Ce qui est vérifié est le graphe où arrivent le concept, sa
 * note, son rattachement au scheme, le lien {@code skos:member} de sa collection, et la collection
 * elle-même : sous Fuseki, un triplet relu sans graphe nommé n'en porte aucun, et la publication
 * partait dans le graphe par défaut sans erreur.
 */
interface ConceptsPublicationScenario {

    /** Le graphe des concepts de {@link GraphsPropertiesStub}, où vivent aussi les collections. */
    String CONCEPTS_GRAPH = "http://rdf.insee.fr/graphes/concepts/";

    String GESTION_BASE = "http://bauhaus/";
    String PUBLICATION_BASE = "http://id.insee.fr/";
    String CONCEPT = GESTION_BASE + "concepts/definition/c9301";
    String NOTE = CONCEPT + "/definition/1/fr";
    String COLLECTION = GESTION_BASE + "concepts/definitions/col9301";
    String PUBLISHED_CONCEPT = PUBLICATION_BASE + "concepts/definition/c9301";
    String PUBLISHED_NOTE = PUBLISHED_CONCEPT + "/definition/1/fr";
    String PUBLISHED_COLLECTION = PUBLICATION_BASE + "concepts/definitions/col9301";
    String PUBLISHED_SCHEME = PUBLICATION_BASE + "concepts/definitions/scheme";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /** Le conteneur n'est vidé qu'entre deux classes : chaque test repart d'un graphe vide. */
    @BeforeEach
    default void dropTheConceptsGraph() throws RmesException {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setUris(new BauhausUriProperties(
                GESTION_BASE, "http://links/", "http://codelist/", "http://documents/", "produits/indicateur"));
        RdfUtils.setDocumentations(new DocumentationsProperties("", "", "concepts/definitions/scheme"));
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder(PUBLICATION_BASE, GESTION_BASE, name -> Optional.of("concepts/definition")));
        gestion().executeUpdate("DROP SILENT GRAPH <%s>".formatted(CONCEPTS_GRAPH));
    }

    @Test
    default void publishes_the_concept_its_note_and_its_collection_link_into_the_concepts_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(conceptLabelled("Ménage"));

        conceptsPublication(gestion).publishConcepts(new JSONArray().put("c9301"), conceptsGraph());

        assertThat(publishedLabels(gestion, PUBLISHED_CONCEPT)).isEqualTo("Ménage");
        assertThat(gestion.getResponseAsBoolean("""
                        ASK { GRAPH <%s> {
                          <%s> <http://www.w3.org/2004/02/skos/core#definition> <%s> ;
                            <http://www.w3.org/2004/02/skos/core#topConceptOf> <%s> .
                          <%3$s> <http://rdf-vocabulary.ddialliance.org/xkos#plainText> "Personnes d'un logement"@fr .
                          <%s> <http://www.w3.org/2004/02/skos/core#member> <%2$s> .
                        } }
                        """.formatted(
                        CONCEPTS_GRAPH, PUBLISHED_CONCEPT, PUBLISHED_NOTE, PUBLISHED_SCHEME, PUBLISHED_COLLECTION)))
                .isTrue();
    }

    @Test
    default void republishing_the_concept_replaces_its_previous_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        ConceptsPublication conceptsPublication = conceptsPublication(gestion);
        gestion.executeUpdate(conceptLabelled("Ménage"));
        conceptsPublication.publishConcepts(new JSONArray().put("c9301"), conceptsGraph());

        relabel(gestion, CONCEPT, "Ménage ordinaire");
        conceptsPublication.publishConcepts(new JSONArray().put("c9301"), conceptsGraph());

        assertThat(publishedLabels(gestion, PUBLISHED_CONCEPT)).isEqualTo("Ménage ordinaire");
    }

    @Test
    default void publishes_the_collection_and_its_members_into_the_concepts_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(collectionLabelled("Démographie"));

        conceptsPublication(gestion).publishCollection(new JSONArray().put("col9301"), conceptsGraph());

        assertThat(publishedLabels(gestion, PUBLISHED_COLLECTION)).isEqualTo("Démographie");
        assertThat(gestion.getResponseAsBoolean(
                        "ASK { GRAPH <%s> { <%s> <http://www.w3.org/2004/02/skos/core#member> <%s> } }"
                                .formatted(CONCEPTS_GRAPH, PUBLISHED_COLLECTION, PUBLISHED_CONCEPT)))
                .isTrue();
    }

    @Test
    default void republishing_the_collection_replaces_its_previous_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        ConceptsPublication conceptsPublication = conceptsPublication(gestion);
        gestion.executeUpdate(collectionLabelled("Démographie"));
        conceptsPublication.publishCollection(new JSONArray().put("col9301"), conceptsGraph());

        relabel(gestion, COLLECTION, "Démographie et population");
        conceptsPublication.publishCollection(new JSONArray().put("col9301"), conceptsGraph());

        assertThat(publishedLabels(gestion, PUBLISHED_COLLECTION)).isEqualTo("Démographie et population");
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private ConceptsPublication conceptsPublication(RepositoryGestion gestion) {
        RepositoryPublication publication = new RepositoryPublication(
                connectionDetails().getUrlServer(), connectionDetails().repositoryId(), repositoryUtils());
        return new ConceptsPublication(
                gestion,
                null,
                publication,
                new PublicationUtils(GESTION_BASE, PUBLICATION_BASE, null, null),
                new ConceptConceptsQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub()),
                new GraphDBCollectionProperties(CONCEPTS_GRAPH, GESTION_BASE + "concepts/definitions"));
    }

    private static String conceptLabelled(String label) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://www.w3.org/2004/02/skos/core#Concept> ;
                    <http://www.w3.org/2004/02/skos/core#notation> "c9301" ;
                    <http://www.w3.org/2004/02/skos/core#prefLabel> "%s"@fr ;
                    <http://rdf.insee.fr/def/base#validationState> "Unpublished" ;
                    <http://www.w3.org/2004/02/skos/core#definition> <%s> .
                  <%4$s> <http://purl.org/dc/terms/language> "fr"^^<http://www.w3.org/2001/XMLSchema#language> ;
                    <http://eurovoc.europa.eu/schema#noteLiteral> "<p>Personnes d'un logement</p>"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral> .
                  <%s> <http://www.w3.org/2004/02/skos/core#member> <%2$s> .
                } }
                """.formatted(CONCEPTS_GRAPH, CONCEPT, label, NOTE, COLLECTION);
    }

    private static String collectionLabelled(String label) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://www.w3.org/2004/02/skos/core#Collection> ;
                    <http://www.w3.org/2004/02/skos/core#prefLabel> "%s"@fr ;
                    <http://rdf.insee.fr/def/base#validationState> "Unpublished" ;
                    <http://www.w3.org/2004/02/skos/core#member> <%s> .
                } }
                """.formatted(CONCEPTS_GRAPH, COLLECTION, label, CONCEPT);
    }

    private static void relabel(RepositoryGestion gestion, String subject, String label) throws RmesException {
        gestion.executeUpdate("""
                DELETE WHERE { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label } } ;
                INSERT DATA { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> "%3$s"@fr } }
                """.formatted(CONCEPTS_GRAPH, subject, label));
    }

    /** Les libellés de l'objet publié lus dans le graphe des concepts, séparés par « | ». */
    private static String publishedLabels(RepositoryGestion gestion, String published) throws RmesException {
        return gestion.getResponseAsObject("""
                        SELECT (GROUP_CONCAT(?label; separator="|") AS ?labels) WHERE {
                          GRAPH <%s> { <%s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label }
                        }
                        """.formatted(CONCEPTS_GRAPH, published))
                .optString("labels");
    }

    private static IRI conceptsGraph() {
        return Values.iri(CONCEPTS_GRAPH);
    }
}
