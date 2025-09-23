package fr.insee.rmes.testcontainers.queries;

import fr.insee.rmes.bauhaus_services.datasets.DatasetQueries;
import fr.insee.rmes.infrastructure.rdf_utils.RepositoryGestion;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
@Tag("integration")
class DatasetQueriesTest extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData(){
        container.withTrigFiles("jeuxDeDonnees-pour-tests.trig");
    }

    @Test
    void should_return_all_datasets() throws Exception {
        DatasetQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(DatasetQueries.getDatasets("http://rdf.insee.fr/graphes/catalogue", null));
        assertEquals(3, result.length());
    }

    @Test
    void should_return_all_datasets_based_on_stamp() throws Exception {
        DatasetQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(DatasetQueries.getDatasets("http://rdf.insee.fr/graphes/catalogue", "DG75-L001"));
        assertEquals(1, result.length());
    }

    @Test
    void should_return_all_archival_units() throws Exception {
        DatasetQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(DatasetQueries.getArchivageUnits());
        assertEquals("diffusion Insee.fr", result.getJSONObject(0).getString("label"));
        assertEquals("http://bauhaus/identifierSchemes/uniteArchivageNamingScheme/identifier/UA1", result.getJSONObject(0).getString("value"));
        assertEquals(1, result.length());
    }
    @Test
    void should_return_all_was_generated_if_multiple_values() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(DatasetQueries.getDatasetWasGeneratedIris("jeuDeDonneesTousChampsEtMultiValeurs", "http://rdf.insee.fr/graphes/catalogue"));
        assertEquals("http://bauhaus/operations/operation/s2159", result.getJSONObject(0).getString("iri"));
        assertEquals("http://bauhaus/operations/operation/s2160", result.getJSONObject(1).getString("iri"));
        assertEquals(2, result.length());
    }

    @Test
    void should_return_all_creators_if_multiple_values() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(DatasetQueries.getDatasetCreators("jeuDeDonneesTousChampsEtMultiValeurs", "http://rdf.insee.fr/graphes/catalogue"));
        assertEquals("http://bauhaus/organisations/ined", result.getJSONObject(0).getString("creator"));
        assertEquals("http://bauhaus/organisations/insee", result.getJSONObject(1).getString("creator"));
        assertEquals(2, result.length());
    }

    @Test
    void should_return_all_spacial_resolutions_if_multiple_values() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(DatasetQueries.getDatasetSpacialResolutions("jeuDeDonneesTousChampsEtMultiValeurs", "http://rdf.insee.fr/graphes/catalogue"));
        assertEquals("http://bauhaus/codes/typeTerritoireGeographique/COM", result.getJSONObject(0).getString("spacialResolution"));
        assertEquals("http://bauhaus/codes/typeTerritoireGeographique/DEP", result.getJSONObject(1).getString("spacialResolution"));
        assertEquals("http://bauhaus/codes/typeTerritoireGeographique/REG", result.getJSONObject(2).getString("spacialResolution"));
        assertEquals(3, result.length());
    }

    @Test
    void should_return_keywords() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(DatasetQueries.getKeywords("jeuDeDonneesTousChamps", "http://rdf.insee.fr/graphes/catalogue"));
        assertEquals("Statistiques", result.getJSONObject(0).getString("keyword"));
        assertEquals("fr", result.getJSONObject(0).getString("lang"));
        assertEquals(1, result.length());
    }

    @Test
    void should_return_linked_documents() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(DatasetQueries.getLinkedDocuments("jeuDeDonneesTousChamps", "http://rdf.insee.fr/graphes/catalogue"));
        assertEquals("https://www.insee.fr/fr/statistiques", result.getJSONObject(0).getString("linkedDocument"));
        assertEquals(1, result.length());
    }
}
