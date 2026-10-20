package fr.insee.rmes.testcontainers.codelists;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.bauhaus_services.code_list.CodeListPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Publication d'une liste de codes, rejouée sur chaque triplestore (voir
 * {@link CodeListPublicationGraphDBIntegrationTest} et {@link CodeListPublicationFusekiIntegrationTest}).
 *
 * <p>Gestion et publication partagent le même dépôt : la liste publiée se distingue par son IRI,
 * réécrite dans la base de publication. Ce qui est vérifié est le graphe où arrivent la liste, la
 * classe OWL qu'elle cite en {@code rdfs:seeAlso} et ses codes : sous Fuseki, un triplet relu sans
 * graphe nommé n'en porte aucun, et la publication partait dans le graphe par défaut sans erreur.
 */
interface CodeListPublicationScenario {

    String CODES_GRAPH = "http://rdf.insee.fr/graphes/codes";
    String GESTION_BASE = "http://bauhaus/";
    String PUBLICATION_BASE = "http://id.insee.fr/";
    String CODE_LIST = GESTION_BASE + "codes/cl1000";
    String OWL_CLASS = GESTION_BASE + "codes/concept/Sexe1000";
    String CODE = GESTION_BASE + "codes/sexe1000/1";
    String PUBLISHED_CODE_LIST = PUBLICATION_BASE + "codes/cl1000";
    String PUBLISHED_OWL_CLASS = PUBLICATION_BASE + "codes/concept/Sexe1000";
    String PUBLISHED_CODE = PUBLICATION_BASE + "codes/sexe1000/1";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /** Le conteneur n'est vidé qu'entre deux classes : chaque test repart d'un graphe vide. */
    @BeforeEach
    default void dropTheCodesGraph() throws RmesException {
        gestion().executeUpdate("DROP SILENT GRAPH <%s>".formatted(CODES_GRAPH));
    }

    @Test
    default void publishes_the_code_list_its_owl_class_and_its_codes_into_the_codes_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(codeListLabelled("Sexe"));

        codeListPublication(gestion).publishCodeListAndCodes(codeList(), codesGraph());

        assertThat(publishedLabels(gestion)).isEqualTo("Sexe");
        assertThat(gestion.getResponseAsBoolean(
                        """
                        ASK { GRAPH <%s> {
                          <%s> <http://www.w3.org/2000/01/rdf-schema#seeAlso> <%s> .
                          <%3$s> a <http://www.w3.org/2002/07/owl#Class> .
                          <%s> a <%3$s> ;
                            <http://www.w3.org/2004/02/skos/core#inScheme> <%2$s> ;
                            <http://www.w3.org/2004/02/skos/core#prefLabel> "Homme"@fr .
                        } }
                        """.formatted(CODES_GRAPH, PUBLISHED_CODE_LIST, PUBLISHED_OWL_CLASS, PUBLISHED_CODE)))
                .isTrue();
    }

    @Test
    default void republishing_the_code_list_replaces_its_previous_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        CodeListPublication codeListPublication = codeListPublication(gestion);
        gestion.executeUpdate(codeListLabelled("Sexe"));
        codeListPublication.publishCodeListAndCodes(codeList(), codesGraph());

        gestion.executeUpdate("""
                DELETE WHERE { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label } } ;
                INSERT DATA { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> "Sexe à la naissance"@fr } }
                """.formatted(CODES_GRAPH, CODE_LIST));
        codeListPublication.publishCodeListAndCodes(codeList(), codesGraph());

        assertThat(publishedLabels(gestion)).isEqualTo("Sexe à la naissance");
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private CodeListPublication codeListPublication(RepositoryGestion gestion) {
        RepositoryPublication publication = new RepositoryPublication(
                connectionDetails().getUrlServer(), connectionDetails().repositoryId(), repositoryUtils());
        return new CodeListPublication(
                gestion, null, publication, new PublicationUtils(GESTION_BASE, PUBLICATION_BASE, null, null));
    }

    private static String codeListLabelled(String label) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://www.w3.org/2004/02/skos/core#ConceptScheme> ;
                    <http://www.w3.org/2004/02/skos/core#notation> "CL1000" ;
                    <http://www.w3.org/2004/02/skos/core#prefLabel> "%s"@fr ;
                    <http://rdf.insee.fr/def/base#validationState> "Unpublished" ;
                    <http://www.w3.org/2000/01/rdf-schema#seeAlso> <%s> .
                  <%4$s> a <http://www.w3.org/2002/07/owl#Class> ;
                    <http://www.w3.org/2000/01/rdf-schema#seeAlso> <%2$s> .
                  <%s> a <%4$s> ;
                    <http://www.w3.org/2004/02/skos/core#inScheme> <%2$s> ;
                    <http://www.w3.org/2004/02/skos/core#notation> "1" ;
                    <http://www.w3.org/2004/02/skos/core#prefLabel> "Homme"@fr .
                } }
                """.formatted(CODES_GRAPH, CODE_LIST, label, OWL_CLASS, CODE);
    }

    /** Les libellés de la liste publiée lus dans le graphe des codes, séparés par « | ». */
    private static String publishedLabels(RepositoryGestion gestion) throws RmesException {
        return gestion.getResponseAsObject("""
                        SELECT (GROUP_CONCAT(?label; separator="|") AS ?labels) WHERE {
                          GRAPH <%s> { <%s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label }
                        }
                        """.formatted(CODES_GRAPH, PUBLISHED_CODE_LIST))
                .optString("labels");
    }

    private static IRI codeList() {
        return Values.iri(CODE_LIST);
    }

    private static IRI codesGraph() {
        return Values.iri(CODES_GRAPH);
    }
}
