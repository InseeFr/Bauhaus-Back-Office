package fr.insee.rmes.testcontainers.documentations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

/**
 * Filet de sécurité E2E : l'ordre des documents d'une rubrique RICHTEXT, stocké
 * sous forme de rdf:List (insee:additionalMaterial -> rdf:first/rdf:rest), doit
 * être préservé lors de la PUBLICATION (recopie gestion -> publication).
 *
 * La publication recopie le graphe du SIMS triplet par triplet
 * (DocumentationPublication#publishSims) : ce test verrouille le fait que les
 * maillons de la liste (noeuds blancs + rdf:first/rdf:rest) sont bien transférés
 * dans le graphe de publication, et dans le bon ordre. Il garde le système
 * honnête si quelqu'un excluait additionalMaterial de la publication ou modifiait
 * transformTripleToPublish.
 */
@Tag("integration")
class SimsDocumentOrderPublicationEndToEndTest extends AbstractSimsPublicationEndToEndTest {

    private static final String PUBLICATION_GRAPH = "http://rdf.insee.fr/graphes/qualite/rapport/7777";
    private static final String PUBLISHED_TEXT_URI = "http://publication/qualite/attribut/7777/S.3.1/texte";

    @BeforeAll
    static void initData() {
        container.withTrigFiles("sims-document-order-publication.trig");
    }

    @Test
    @DisplayName("La publication d'un SIMS conserve l'ordre de la liste de documents")
    void publishingASimsKeepsDocumentListOrder() throws Exception, MissingUserInformationException {
        ResponseEntity<String> response = validateSimsAsAdmin("7777");

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        // Reconstitue l'ordre de la rdf:List dans le graphe de publication : la
        // position d'un maillon = nombre de maillons depuis la tête jusqu'à lui.
        // IRI complètes (le repository préfixe déjà la requête avec rdf:, insee:...).
        String orderedListQuery = """
                SELECT ?doc (COUNT(?mid) AS ?pos) WHERE {
                    GRAPH <%s> {
                        <%s> <http://rdf.insee.fr/def/base#additionalMaterial> ?head .
                        ?head <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>* ?member .
                        ?member <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?doc .
                        ?head <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>* ?mid .
                        ?mid <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>* ?member .
                    }
                }
                GROUP BY ?doc
                ORDER BY ?pos
                """.formatted(PUBLICATION_GRAPH, PUBLISHED_TEXT_URI);

        JSONArray published = repositoryPublication.getResponseAsArray(orderedListQuery);

        List<String> publishedDocs = new ArrayList<>(published.length());
        JSONUtils.stream(published).map(row -> row.getString("doc")).forEach(publishedDocs::add);

        assertThat(publishedDocs)
                .as("Les documents doivent être publiés dans le graphe de publication, "
                        + "avec leur IRI de publication, et dans l'ordre de la liste RDF")
                .containsExactly(
                        "http://publication/documents/document/801",
                        "http://publication/documents/document/802",
                        "http://publication/documents/document/803");
    }
}
