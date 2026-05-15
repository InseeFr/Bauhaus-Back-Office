package fr.insee.rmes.testcontainers.documentations;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Filet de sécurité E2E pour l'ordre des documents dans une rubrique RICHTEXT.
 *
 * Ces scénarios verrouillent le comportement attendu : la persistance d'un
 * SIMS doit conserver l'ordre des documents associés à une rubrique. La forme
 * RDF cible est la liste ordonnée (rdf:first/rdf:rest), attachée au noeud
 * texte de la rubrique via insee:additionalMaterial.
 *
 * Le test est RED tant que la lecture ne reconstruit pas la liste ordonnée
 * (étape 2 du plan DOCUMENT_ORDER.md).
 */
@Tag("integration")
class DocumentRubricOrderEndToEndTest extends WithGraphDBContainer {

    private static final String SIMS_ID = "9999";
    private static final String RUBRIC_ID = "S.3.1";
    private static final String LANG_FR = "fr";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED)
    );

    private OperationDocumentsQueries operationDocumentsQueries;

    @BeforeAll
    static void initData() {
        container.withTrigFiles("sims-document-order.trig");
    }

    @BeforeEach
    void setUp() {
        operationDocumentsQueries = new OperationDocumentsQueries(
                BauhausUriPropertiesStub.stub(),
                new BauhausLanguagesProperties("fr", "en"),
                GraphsPropertiesStub.stub());
    }

    @Test
    @DisplayName("Une rubrique stockée en rdf:List est lue dans l'ordre de la liste")
    void shouldReadDocumentsInListOrder() throws Exception {
        String query = operationDocumentsQueries.getDocumentsForSimsRubricQuery(
                SIMS_ID, RUBRIC_ID, "http://bauhaus/codes/langue/" + LANG_FR);

        JSONArray docs = repositoryGestion.getResponseAsArray(query);

        List<String> ids = idsOf(docs);
        assertThat(ids)
                .as("Les documents doivent être restitués dans l'ordre de la liste RDF (709, 710, 711)")
                .containsExactly("709", "710", "711");
    }

    @Test
    @DisplayName("Tous les documents de la liste sont restitués (pas de perte)")
    void shouldReturnAllDocumentsInList() throws Exception {
        String query = operationDocumentsQueries.getDocumentsForSimsRubricQuery(
                SIMS_ID, RUBRIC_ID, "http://bauhaus/codes/langue/" + LANG_FR);

        JSONArray docs = repositoryGestion.getResponseAsArray(query);

        assertThat(docs.length())
                .as("Les 3 documents de la liste doivent être présents")
                .isEqualTo(3);
    }

    private static List<String> idsOf(JSONArray docs) {
        List<String> ids = new ArrayList<>(docs.length());
        for (int i = 0; i < docs.length(); i++) {
            ids.add(docs.getJSONObject(i).getString(Constants.ID));
        }
        return ids;
    }
}
