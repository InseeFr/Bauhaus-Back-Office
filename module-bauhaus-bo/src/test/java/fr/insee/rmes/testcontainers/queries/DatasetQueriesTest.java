package fr.insee.rmes.testcontainers.queries;

import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetQueries;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@Tag("integration")
class DatasetQueriesTest extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    DatasetQueries datasetQueries = new DatasetQueries(new BauhausLanguagesProperties("fr", "en"));

    private static final String DUPLICATE_IDENTIFIER_GRAPH = "http://rdf.insee.fr/graphes/catalogue-identifiant-duplique";

    @BeforeAll
    static void initData(){
        container.withTrigFiles("jeuxDeDonnees-pour-tests.trig");
        container.withTrigFiles("jeuxDeDonnees-identifiant-duplique.trig");
    }

    @Test
    void should_return_all_datasets() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getDatasets("http://rdf.insee.fr/graphes/catalogue", Set.of()));
        assertEquals(3, result.length());
    }

    @Test
    void should_return_one_row_per_dataset_even_when_two_iris_share_the_same_identifier() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getDatasets(DUPLICATE_IDENTIFIER_GRAPH, Set.of()));

        assertEquals(1, result.length(), "The list must expose one row per dataset identifier");
        assertEquals("jeuDeDonneesDedouble", result.getJSONObject(0).getString("id"));
    }

    @Test
    void should_expose_alt_identifier_for_search() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getDatasetsForSearch("http://rdf.insee.fr/graphes/catalogue", "http://rdf.insee.fr/graphes/adms"));
        Set<String> altIdentifiers = new HashSet<>();
        for (int i = 0; i < result.length(); i++) {
            if (result.getJSONObject(i).has("altIdentifier")) {
                altIdentifiers.add(result.getJSONObject(i).getString("altIdentifier"));
            }
        }
        assertTrue(
                altIdentifiers.contains("DATASET_ALL_PROPERTIES"),
                "Expected altIdentifier DATASET_ALL_PROPERTIES, got: " + altIdentifiers
        );
        assertTrue(
                altIdentifiers.contains("DATASET_ALL_PROPERTIES_WITH_MULTIPLE_VALUES"),
                "Expected altIdentifier DATASET_ALL_PROPERTIES_WITH_MULTIPLE_VALUES, got: " + altIdentifiers
        );
    }

    @Test
    void should_return_one_row_per_dataset_for_search_even_with_multivalued_properties() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getDatasetsForSearch("http://rdf.insee.fr/graphes/catalogue", "http://rdf.insee.fr/graphes/adms"));

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < result.length(); i++) {
            ids.add(result.getJSONObject(i).getString("id"));
        }

        // Une ligne par dataset : pas de duplication due au produit cartésien des OPTIONAL multi-valués.
        assertEquals(ids.size(), result.length(), "Search must return exactly one row per dataset");
        assertEquals(3, result.length());
    }

    @Test
    void should_aggregate_was_generated_iris_in_search_for_multivalued_dataset() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getDatasetsForSearch("http://rdf.insee.fr/graphes/catalogue", "http://rdf.insee.fr/graphes/adms"));

        String wasGeneratedIRIs = null;
        for (int i = 0; i < result.length(); i++) {
            JSONObject row = result.getJSONObject(i);
            if ("jeuDeDonneesTousChampsEtMultiValeurs".equals(row.getString("id"))) {
                wasGeneratedIRIs = row.optString("wasGeneratedIRIs");
            }
        }

        assertTrue(wasGeneratedIRIs != null && wasGeneratedIRIs.contains("http://bauhaus/operations/operation/s2159"));
        assertTrue(wasGeneratedIRIs != null && wasGeneratedIRIs.contains("http://bauhaus/operations/operation/s2160"));
    }

    @Test
    void should_return_all_datasets_based_on_stamp() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getDatasets("http://rdf.insee.fr/graphes/catalogue", Set.of("DG75-L001")));
        assertEquals(1, result.length());
    }

    @Test
    void should_return_all_archival_units() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getArchivageUnits());
        assertEquals("diffusion Insee.fr", result.getJSONObject(0).getString("label"));
        assertEquals("http://bauhaus/identifierSchemes/uniteArchivageNamingScheme/identifier/UA1", result.getJSONObject(0).getString("value"));
        assertEquals(1, result.length());
    }
    @Test
    void should_return_all_was_generated_if_multiple_values() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getDatasetWasGeneratedIris("jeuDeDonneesTousChampsEtMultiValeurs", "http://rdf.insee.fr/graphes/catalogue"));
        assertEquals("http://bauhaus/operations/operation/s2159", result.getJSONObject(0).getString("iri"));
        assertEquals("http://bauhaus/operations/operation/s2160", result.getJSONObject(1).getString("iri"));
        assertEquals(2, result.length());
    }

    @Test
    void should_return_all_creators_if_multiple_values() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getDatasetCreators("jeuDeDonneesTousChampsEtMultiValeurs", "http://rdf.insee.fr/graphes/catalogue"));
        assertEquals("http://bauhaus/organisations/ined", result.getJSONObject(0).getString("creator"));
        assertEquals("http://bauhaus/organisations/insee", result.getJSONObject(1).getString("creator"));
        assertEquals(2, result.length());
    }

    @Test
    void should_return_all_spacial_resolutions_if_multiple_values() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getDatasetSpacialResolutions("jeuDeDonneesTousChampsEtMultiValeurs", "http://rdf.insee.fr/graphes/catalogue"));
        assertEquals("http://bauhaus/codes/typeTerritoireGeographique/COM", result.getJSONObject(0).getString("spacialResolution"));
        assertEquals("http://bauhaus/codes/typeTerritoireGeographique/DEP", result.getJSONObject(1).getString("spacialResolution"));
        assertEquals("http://bauhaus/codes/typeTerritoireGeographique/REG", result.getJSONObject(2).getString("spacialResolution"));
        assertEquals(3, result.length());
    }

    @Test
    void should_return_keywords() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getKeywords("jeuDeDonneesTousChamps", "http://rdf.insee.fr/graphes/catalogue"));
        assertEquals("Statistiques", result.getJSONObject(0).getString("keyword"));
        assertEquals("fr", result.getJSONObject(0).getString("lang"));
        assertEquals(1, result.length());
    }

    @Test
    void should_return_linked_documents() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(datasetQueries.getLinkedDocuments("jeuDeDonneesTousChamps", "http://rdf.insee.fr/graphes/catalogue"));
        assertEquals("https://www.insee.fr/fr/statistiques", result.getJSONObject(0).getString("linkedDocument"));
        assertEquals(1, result.length());
    }
}
