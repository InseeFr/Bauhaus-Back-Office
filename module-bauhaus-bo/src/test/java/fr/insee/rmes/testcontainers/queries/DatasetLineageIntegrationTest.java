package fr.insee.rmes.testcontainers.queries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.datasets.DatasetServiceImpl;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesRepository;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.datasets.datasets.model.WasDerivedFrom;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetDistributionQueries;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import fr.insee.rmes.utils.IdGenerator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Lignage entre jeux de données (Bauhaus-Back-Office#903) : un {@code prov:wasDerivedFrom} par jeu
 * source, et un nœud blanc {@code prov:qualifiedDerivation} seulement quand une description est
 * saisie. Le nœud doit disparaître en entier quand la description est retirée ou le jeu supprimé.
 */
@Tag("integration")
class DatasetLineageIntegrationTest extends WithGraphDBContainer {

    private static final String BASE_GRAPH = "http://rdf.insee.fr/graphes/";
    private static final String DATASETS_GRAPH = "catalogue-lignage-it";
    private static final String GRAPH = BASE_GRAPH + DATASETS_GRAPH;
    private static final String BASE_URI = "http://bauhaus";
    private static final String DATASETS_BASE_URI = "/catalogues/jeuDeDonnees";
    private static final String DATASET_IRI_PREFIX = BASE_URI + DATASETS_BASE_URI + "/";
    private static final String PROV = "http://www.w3.org/ns/prov#";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private DatasetServiceImpl service;

    @BeforeEach
    void createService() throws RmesException {
        BauhausLanguagesProperties languages = new BauhausLanguagesProperties("fr", "en");
        SeriesRepository seriesRepository = mock(SeriesRepository.class);
        when(seriesRepository.isSeriesAndOperationsExist(any())).thenReturn(true);

        service = new DatasetServiceImpl(
                repositoryGestion,
                mock(IdGenerator.class),
                mock(RepositoryPublication.class),
                languages,
                mock(PublicationUtils.class),
                seriesRepository,
                new DatasetQueries(languages),
                new DatasetDistributionQueries(languages),
                mock(OrganizationsService.class),
                DATASETS_GRAPH,
                DATASETS_BASE_URI,
                "/catalogues/entreeCatalogue",
                BASE_GRAPH,
                BASE_URI,
                "/catalogues/distribution",
                "adms-lignage-it",
                "/identifiantsAlternatifs/jeuDeDonnees",
                mock(ApplicationEventPublisher.class));
    }

    @Test
    void links_each_source_dataset_without_qualified_derivation_when_no_description() throws RmesException {
        saveSources("src-a1", "src-a2");

        service.update("derive-a", datasetBody("derive-a", lineage(List.of("src-a1", "src-a2"), null, null)));

        assertThat(derivedFromIris("derive-a"))
                .containsExactlyInAnyOrder(DATASET_IRI_PREFIX + "src-a1", DATASET_IRI_PREFIX + "src-a2");
        assertThat(derivationNodeCount("derive-a")).isZero();

        WasDerivedFrom read = service.getDatasetByID("derive-a").getWasDerivedFrom();
        assertThat(read.datasets()).containsExactlyInAnyOrder("src-a1", "src-a2");
        assertThat(read.descriptionLg1()).isNull();
        assertThat(read.descriptionLg2()).isNull();
    }

    @Test
    void stores_a_single_qualified_derivation_holding_every_source_and_the_bilingual_description()
            throws RmesException {
        saveSources("src-b1", "src-b2");

        service.update(
                "derive-b",
                datasetBody("derive-b", lineage(List.of("src-b1", "src-b2"), "Construit à partir", "Built from")));

        JSONArray derivation = repositoryGestion.getResponseAsArray("SELECT ?entity ?description WHERE { GRAPH <"
                + GRAPH + "> { <" + DATASET_IRI_PREFIX + "derive-b> <" + PROV + "qualifiedDerivation> ?n ."
                + " ?n a <" + PROV + "Derivation> ;"
                + " <" + PROV + "entity> ?entity ;"
                + " <http://purl.org/dc/terms/description> ?description . } }");
        assertThat(valuesOf(derivation, "entity"))
                .containsOnly(DATASET_IRI_PREFIX + "src-b1", DATASET_IRI_PREFIX + "src-b2");
        assertThat(valuesOf(derivation, "description")).containsOnly("Construit à partir", "Built from");
        assertThat(derivationNodeCount("derive-b")).isEqualTo(1);

        WasDerivedFrom read = service.getDatasetByID("derive-b").getWasDerivedFrom();
        assertThat(read.datasets()).containsExactlyInAnyOrder("src-b1", "src-b2");
        assertThat(read.descriptionLg1()).isEqualTo("Construit à partir");
        assertThat(read.descriptionLg2()).isEqualTo("Built from");
    }

    @Test
    void removes_the_whole_qualified_derivation_when_the_description_is_removed() throws RmesException {
        saveSources("src-c1");
        service.update("derive-c", datasetBody("derive-c", lineage(List.of("src-c1"), "Description", "Description")));

        service.update("derive-c", datasetBody("derive-c", lineage(List.of("src-c1"), null, null)));

        assertThat(derivationNodeCount("derive-c")).isZero();
        assertThat(orphanDerivationTripleCount()).isZero();
        assertThat(derivedFromIris("derive-c")).containsExactly(DATASET_IRI_PREFIX + "src-c1");
    }

    @Test
    void replaces_the_qualified_derivation_when_the_description_changes() throws RmesException {
        saveSources("src-d1");
        service.update("derive-d", datasetBody("derive-d", lineage(List.of("src-d1"), "Avant", "Before")));

        service.update("derive-d", datasetBody("derive-d", lineage(List.of("src-d1"), "Après", "After")));

        assertThat(derivationNodeCount("derive-d")).isEqualTo(1);
        assertThat(orphanDerivationTripleCount()).isZero();
        WasDerivedFrom read = service.getDatasetByID("derive-d").getWasDerivedFrom();
        assertThat(read.descriptionLg1()).isEqualTo("Après");
        assertThat(read.descriptionLg2()).isEqualTo("After");
    }

    @Test
    void ignores_a_description_given_without_any_source_dataset() throws RmesException {
        service.update("derive-f", datasetBody("derive-f", lineage(List.of(), "Orpheline", "Orphan")));

        assertThat(derivationNodeCount("derive-f")).isZero();
        assertThat(service.getDatasetByID("derive-f").getWasDerivedFrom()).isNull();
    }

    @Test
    void deleting_a_derived_dataset_removes_the_whole_qualified_derivation() throws RmesException {
        saveSources("src-e1");
        service.update("derive-e", datasetBody("derive-e", lineage(List.of("src-e1"), "Description", "Description")));

        service.deleteDatasetId("derive-e");

        assertThat(orphanDerivationTripleCount()).isZero();
    }

    @Test
    void rejects_a_dataset_derived_from_itself() throws RmesException {
        saveSources("self-k");

        assertThatThrownBy(() ->
                        service.update("self-k", datasetBody("self-k", lineage(List.of("self-k"), null, null))))
                .isInstanceOf(RmesBadRequestException.class)
                .extracting(e -> ((RmesException) e).getDetails())
                .asString()
                .contains("\"code\":1206");
        assertThat(derivedFromIris("self-k")).isEmpty();
    }

    @Test
    void rejects_unknown_source_datasets_without_changing_the_stored_lineage() throws RmesException {
        saveSources("src-l1");
        service.update("derive-l", datasetBody("derive-l", lineage(List.of("src-l1"), "Avant", "Before")));

        assertThatThrownBy(() -> service.update(
                        "derive-l", datasetBody("derive-l", lineage(List.of("src-l1", "inconnu-l"), null, null))))
                .isInstanceOf(RmesBadRequestException.class)
                .extracting(e -> ((RmesException) e).getDetails())
                .asString()
                .contains("\"code\":1207")
                .contains("inconnu-l");
        assertThat(service.getDatasetByID("derive-l").getWasDerivedFrom())
                .isEqualTo(new WasDerivedFrom(List.of("src-l1"), "Avant", "Before"));
    }

    @Test
    void links_a_source_through_its_stored_iri_even_when_it_does_not_follow_the_naming_scheme() throws RmesException {
        insertData("<http://ancien-schema/jeu/legacy-g1> a <http://www.w3.org/ns/dcat#Dataset> ;"
                + " <http://purl.org/dc/terms/identifier> \"legacy-g1\" .");

        service.update("derive-g", datasetBody("derive-g", lineage(List.of("legacy-g1"), null, null)));

        assertThat(derivedFromIris("derive-g")).containsExactly("http://ancien-schema/jeu/legacy-g1");
        assertThat(service.getDatasetByID("derive-g").getWasDerivedFrom().datasets())
                .containsExactly("legacy-g1");
    }

    @Test
    void keeps_the_links_to_resources_outside_the_catalogue_when_saving() throws RmesException {
        saveSources("src-h1");
        service.update("derive-h", datasetBody("derive-h", null));
        insertData("<" + DATASET_IRI_PREFIX + "derive-h> <" + PROV + "wasDerivedFrom> <http://externe/h> .");

        WasDerivedFrom read = service.getDatasetByID("derive-h").getWasDerivedFrom();
        service.update(
                "derive-h", datasetBody("derive-h", lineage(List.of("src-h1"), "Avec une source externe", null)));

        assertThat(read).isNull();
        assertThat(derivedFromIris("derive-h"))
                .containsExactlyInAnyOrder(DATASET_IRI_PREFIX + "src-h1", "http://externe/h");
        assertThat(entitiesOf("derive-h")).containsExactlyInAnyOrder(DATASET_IRI_PREFIX + "src-h1", "http://externe/h");
    }

    @Test
    void reads_a_description_without_language_as_the_first_language_one() throws RmesException {
        saveSources("src-i1");
        service.update("derive-i", datasetBody("derive-i", lineage(List.of("src-i1"), null, null)));
        insertData("<" + DATASET_IRI_PREFIX + "derive-i> <" + PROV + "qualifiedDerivation> _:n ."
                + " _:n a <" + PROV + "Derivation> ; <" + PROV + "entity> <" + DATASET_IRI_PREFIX + "src-i1> ;"
                + " <http://purl.org/dc/terms/description> \"Sans langue\" .");

        assertThat(service.getDatasetByID("derive-i").getWasDerivedFrom().descriptionLg1())
                .isEqualTo("Sans langue");
    }

    @Test
    void keeps_the_descriptions_written_in_other_languages_when_saving() throws RmesException {
        saveSources("src-j1");
        service.update("derive-j", datasetBody("derive-j", lineage(List.of("src-j1"), "Avant", null)));
        insertData(
                "?d <" + PROV + "qualifiedDerivation> ?n .",
                "?n <http://purl.org/dc/terms/description> \"Vorher\"@de .",
                "<" + DATASET_IRI_PREFIX + "derive-j>");

        service.update("derive-j", datasetBody("derive-j", lineage(List.of("src-j1"), "Après", "After")));

        assertThat(descriptionsOf("derive-j")).containsExactlyInAnyOrder("Après", "After", "Vorher");
    }

    private void saveSources(String... ids) throws RmesException {
        for (String id : ids) {
            service.update(id, datasetBody(id, null));
        }
    }

    private static JSONObject lineage(List<String> datasets, String descriptionLg1, String descriptionLg2) {
        JSONObject lineage = new JSONObject().put("datasets", datasets);
        if (descriptionLg1 != null) {
            lineage.put("descriptionLg1", descriptionLg1);
        }
        if (descriptionLg2 != null) {
            lineage.put("descriptionLg2", descriptionLg2);
        }
        return lineage;
    }

    private static String datasetBody(String id, JSONObject wasDerivedFrom) {
        JSONObject body = new JSONObject()
                .put("id", id)
                .put("labelLg1", "Jeu " + id)
                .put("labelLg2", "Dataset " + id)
                .put("disseminationStatus", "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique")
                .put("validationState", "Unpublished")
                .put(
                        "catalogRecord",
                        new JSONObject()
                                .put("creator", "http://bauhaus/organisations/insee/HIE")
                                .put("contributor", List.of("http://bauhaus/organisations/insee/HIE")));
        if (wasDerivedFrom != null) {
            body.put("wasDerivedFrom", wasDerivedFrom);
        }
        return body.toString();
    }

    private void insertData(String triples) throws RmesException {
        repositoryGestion.executeUpdate("INSERT DATA { GRAPH <" + GRAPH + "> { " + triples + " } }");
    }

    /** Ajoute {@code inserted} au nœud de dérivation du jeu {@code dataset}, repéré par {@code pattern}. */
    private void insertData(String pattern, String inserted, String dataset) throws RmesException {
        repositoryGestion.executeUpdate("INSERT { GRAPH <" + GRAPH + "> { " + inserted + " } } WHERE { GRAPH <" + GRAPH
                + "> { " + pattern + " } VALUES ?d { " + dataset + " } }");
    }

    private List<String> entitiesOf(String id) throws RmesException {
        return valuesOf(
                repositoryGestion.getResponseAsArray("SELECT ?entity WHERE { GRAPH <" + GRAPH + "> { <"
                        + DATASET_IRI_PREFIX + id + "> <" + PROV + "qualifiedDerivation> ?n ."
                        + " ?n <" + PROV + "entity> ?entity . } }"),
                "entity");
    }

    private List<String> descriptionsOf(String id) throws RmesException {
        return valuesOf(
                repositoryGestion.getResponseAsArray("SELECT ?description WHERE { GRAPH <" + GRAPH + "> { <"
                        + DATASET_IRI_PREFIX + id + "> <" + PROV + "qualifiedDerivation> ?n ."
                        + " ?n <http://purl.org/dc/terms/description> ?description . } }"),
                "description");
    }

    private List<String> derivedFromIris(String id) throws RmesException {
        return valuesOf(
                repositoryGestion.getResponseAsArray("SELECT ?source WHERE { GRAPH <" + GRAPH + "> { <"
                        + DATASET_IRI_PREFIX + id + "> <" + PROV + "wasDerivedFrom> ?source . } }"),
                "source");
    }

    private int derivationNodeCount(String id) throws RmesException {
        return repositoryGestion
                .getResponseAsArray("SELECT ?n WHERE { GRAPH <" + GRAPH + "> { <" + DATASET_IRI_PREFIX + id + "> <"
                        + PROV + "qualifiedDerivation> ?n . ?n a <" + PROV + "Derivation> . } }")
                .length();
    }

    /** Triplets d'un nœud de dérivation qui ne serait plus rattaché à aucun jeu de données. */
    private int orphanDerivationTripleCount() throws RmesException {
        return repositoryGestion
                .getResponseAsArray("SELECT ?n WHERE { GRAPH <" + GRAPH + "> {"
                        + " { ?n <" + PROV + "entity> ?o } UNION { ?n <http://purl.org/dc/terms/description> ?o }"
                        + " FILTER (isBlank(?n))"
                        + " FILTER NOT EXISTS { ?d <" + PROV + "qualifiedDerivation> ?n } } }")
                .length();
    }

    private static List<String> valuesOf(JSONArray rows, String key) {
        return JSONUtils.stream(rows).map(row -> row.getString(key)).toList();
    }
}
