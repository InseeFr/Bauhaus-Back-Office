package fr.insee.rmes.testcontainers.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Filet de sécurité « CRUD nomenclatures » du lot A de la migration GraphDB → Fuseki.
 *
 * <p>Avant ce test, les classifications n'étaient couvertes que par des tests mockés : aucun
 * aller-retour réel contre un triplestore. Or les templates SPARQL des nomenclatures
 * (lecture sans clause {@code GRAPH}, lecture du {@code validationState} dans le graphe
 * « nomenclatures », jointures via {@code skos:inScheme}/{@code skos:member}/{@code xkos:depth})
 * sont exactement ce qui doit se comporter à l'identique sur le futur backend.
 *
 * <p>Le test exerce, contre un vrai GraphDB et via les <em>vraies</em> requêtes
 * {@link ClassificationsQueries} :
 * <ul>
 *   <li>la lecture d'une nomenclature et de son état de validation ;</li>
 *   <li>le listing de toutes les nomenclatures ;</li>
 *   <li>la lecture des postes (items) d'une nomenclature ;</li>
 *   <li>un aller-retour écriture→lecture : un item ajouté via {@link RepositoryGestion}
 *       devient visible à travers la requête de lecture des items.</li>
 * </ul>
 *
 * <p>NB : les requêtes de lecture interrogent le graphe par défaut, que GraphDB matérialise
 * comme l'union de tous les graphes nommés. C'est une dépendance au backend (Fuseki sépare
 * par défaut graphe par défaut et graphes nommés) que la suite croisée du lot E re-testera.
 */
@Tag("integration")
class ClassificationsCrudIntegrationTest extends WithGraphDBContainer {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final String CLASSIF_ID = "nafr2-it";
    private static final Resource CODES_GRAPH = VF.createIRI("http://rdf.insee.fr/graphes/codes/" + CLASSIF_ID);
    private static final IRI CLASSIFICATION = VF.createIRI("http://rdf.insee.fr/codes/" + CLASSIF_ID + "/");
    private static final IRI LEVEL = VF.createIRI("http://rdf.insee.fr/codes/" + CLASSIF_ID + "/niveau/1");

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    private final ClassificationsQueries classificationsQueries =
            new ClassificationsQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @BeforeAll
    static void initData() {
        container.withTrigFiles("classifications-crud-it.trig");
    }

    @Test
    void classificationQuery_returns_the_seeded_classification_with_its_validation_state() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(classificationsQueries.classificationQuery(CLASSIF_ID));

        assertThat(result.getString("id")).isEqualTo(CLASSIF_ID);
        assertThat(result.getString("prefLabelLg1")).isEqualTo("NAF rév. 2 (test)");
        assertThat(result.getString("validationState"))
                .as("le validationState est lu dans le graphe nomenclatures")
                .isEqualTo("Validated");
    }

    @Test
    void classificationsQuery_lists_the_seeded_classification() throws RmesException {
        JSONArray result = repositoryGestion.getResponseAsArray(classificationsQueries.classificationsQuery());

        assertThat(valuesOf(result, "id")).contains(CLASSIF_ID);
    }

    @Test
    void classificationItemsQuery_returns_the_seeded_item() throws RmesException {
        JSONArray items = repositoryGestion.getResponseAsArray(classificationsQueries.classificationItemsQuery(CLASSIF_ID));

        assertThat(items).isNotNull();
        assertThat(valuesOf(items, "id")).contains("01");
        assertThat(items.getJSONObject(0).getString("labelLg1")).isEqualTo("Agriculture (test)");
    }

    @Test
    void item_written_via_repositoryGestion_becomes_visible_through_the_items_query() throws RmesException {
        IRI newItem = VF.createIRI("http://rdf.insee.fr/codes/" + CLASSIF_ID + "/02/");
        Model model = new LinkedHashModel();
        model.add(newItem, RDF.TYPE, SKOS.CONCEPT, CODES_GRAPH);
        model.add(newItem, SKOS.IN_SCHEME, CLASSIFICATION, CODES_GRAPH);
        model.add(newItem, SKOS.PREF_LABEL, VF.createLiteral("Pêche (test)", "fr"), CODES_GRAPH);
        model.add(newItem, SKOS.NOTATION, VF.createLiteral("02"), CODES_GRAPH);
        // rattache le nouvel item au niveau existant (le template exige ?level skos:member ?item)
        model.add(LEVEL, SKOS.MEMBER, newItem, CODES_GRAPH);

        repositoryGestion.loadSimpleObjectWithoutDeletion(newItem, model, null);

        JSONArray items = repositoryGestion.getResponseAsArray(classificationsQueries.classificationItemsQuery(CLASSIF_ID));
        assertThat(valuesOf(items, "id"))
                .as("le poste ajouté est lu par la requête des items")
                .contains("01", "02");
    }

    private static List<String> valuesOf(JSONArray array, String key) {
        List<String> values = new ArrayList<>();
        if (array != null) {
            JSONUtils.stream(array).forEach(row -> {
                if (row.has(key)) {
                    values.add(row.getString(key));
                }
            });
        }
        return values;
    }
}
