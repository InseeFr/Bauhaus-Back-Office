package fr.insee.rmes.modules.users.infrastructure.stamps;

import static fr.insee.rmes.PropertiesKeys.CONCEPTS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.OPERATIONS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.OP_FAMILIES_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.OP_SERIES_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.PRODUCTS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.STRUCTURES_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.STRUCTURES_COMPONENTS_BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb.GraphDBCollectionProperties;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationRepository;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.modules.users.domain.exceptions.StampFetchException;
import fr.insee.rmes.modules.users.domain.exceptions.UnsupportedModuleException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetDistributionQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphDbStampCheckerTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final String CONCEPTS_GRAPH = GraphsPropertiesStub.stub().conceptsGraph();
    private static final String SERIES_ID = "s42";
    private static final IRI SERIES_IRI =
            SimpleValueFactory.getInstance().createIRI("http://bauhaus/operations/serie/" + SERIES_ID);

    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private StructureQueries structureQueries;

    @Mock
    private OperationSeriesQueries operationSeriesQueries;

    @Mock
    private DDIRepository ddiRepository;

    @Mock
    private SeriesCreatorsPort seriesCreatorsPort;

    @Mock
    private DatasetDistributionQueries datasetDistributionQueries;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private ContributorsQueries contributorsQueries;

    @Mock
    private ClassificationsQueries classificationsQueries;

    @Mock
    private CodeListsQueries codeListsQueries;

    private final GraphDBCollectionProperties collectionProperties =
            new GraphDBCollectionProperties("http://bauhaus/graph/concepts", "http://bauhaus/concepts/definitions");

    /** Fabrique réelle : les IRI qu'elle construit font partie de ce que ces tests vérifient. */
    private final BauhausIriFactory iriFactory = new BauhausIriFactory(
            GraphsPropertiesStub.stub(),
            new BauhausUriBuilder("http://publication/", "http://bauhaus/", name -> switch (name) {
                case CONCEPTS_BASE_URI -> Optional.of("concepts/definition");
                case OP_FAMILIES_BASE_URI -> Optional.of("operations/famille");
                case OP_SERIES_BASE_URI -> Optional.of("operations/serie");
                case OPERATIONS_BASE_URI -> Optional.of("operations/operation");
                case PRODUCTS_BASE_URI -> Optional.of("produits/indicateur");
                case STRUCTURES_BASE_URI -> Optional.of("structures/structure");
                case STRUCTURES_COMPONENTS_BASE_URI -> Optional.of("structures/");
                default -> Optional.empty();
            }));

    private static final String DATASETS_GRAPH = "http://rdf.insee.fr/graphes/catalogue";
    private static final String CATALOG_RECORD_BASE_URI = "http://bauhaus/catalogues/entreeCatalogue";

    private GraphDbStampChecker checker;

    @BeforeEach
    void setUp() {
        checker = new GraphDbStampChecker(
                repositoryGestion,
                structureQueries,
                operationSeriesQueries,
                ddiRepository,
                seriesCreatorsPort,
                datasetDistributionQueries,
                organisationRepository,
                contributorsQueries,
                classificationsQueries,
                codeListsQueries,
                GraphsPropertiesStub.stub(),
                collectionProperties,
                iriFactory,
                DATASETS_GRAPH,
                CATALOG_RECORD_BASE_URI);
    }

    /** Les contributeurs de la ressource d'IRI donnée, lus dans le graphe donné, une ligne par valeur. */
    private void givenContributorsOf(String graph, String iri, String... contributors) throws RmesException {
        String query = "SELECT-contributors-" + iri;
        when(contributorsQueries.getContributors(graph, VF.createIRI(iri))).thenReturn(query);
        JSONArray rows = new JSONArray();
        for (String contributor : contributors) {
            rows.put(new JSONObject().put("contributor", contributor));
        }
        when(repositoryGestion.getResponseAsArray(query)).thenReturn(rows);
    }

    private static Ddi4Group group(List<Reference> studyUnitReference, List<String> seriesIris) {
        return new Ddi4Group(
                Ddi4Group.TYPE,
                null,
                null,
                "fr.insee",
                "group-id",
                "1",
                null,
                null,
                studyUnitReference,
                seriesIris,
                null);
    }

    private void givenGroupResponse(Ddi4GroupResponse groupResponse) {
        when(ddiRepository.getGroup("fr.insee", "group-id")).thenReturn(groupResponse);
    }

    private List<String> creatorsStampsOfGroup() throws StampFetchException, UnsupportedModuleException {
        return checker.getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, "fr.insee|group-id");
    }

    /** La requête des créateurs de la série SERIES_ID renvoie une ligne par valeur donnée. */
    private void givenSeriesCreators(String... creators) throws RmesException {
        String query = "SELECT-creators";
        when(operationSeriesQueries.getCreatorsBySeriesUri(SERIES_IRI.toString()))
                .thenReturn(query);
        JSONArray rows = new JSONArray();
        for (String creator : creators) {
            rows.put(new JSONObject().put("creators", creator));
        }
        when(repositoryGestion.getResponseAsArray(query)).thenReturn(rows);
    }

    private List<String> creatorsStampsOfSeries() throws StampFetchException, UnsupportedModuleException {
        return checker.getCreatorsStamps(RBAC.Module.OPERATION_SERIES, SERIES_ID);
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_returns_empty_list_when_id_is_null()
            throws StampFetchException, UnsupportedModuleException {
        List<String> result = checker.getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, null);

        assertThat(result).isEmpty();
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_returns_empty_list_when_id_has_no_delimiter()
            throws StampFetchException, UnsupportedModuleException {
        List<String> result = checker.getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, "group-id-only");

        assertThat(result).isEmpty();
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_returns_empty_list_when_group_has_no_series()
            throws StampFetchException, UnsupportedModuleException {
        givenGroupResponse(new Ddi4GroupResponse(null, null, List.of(group(List.of(), List.of())), null));

        List<String> result = creatorsStampsOfGroup();

        assertThat(result).isEmpty();
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_returns_creators_when_series_have_creators()
            throws StampFetchException, UnsupportedModuleException {
        String iri1 = "http://id.insee.fr/operations/serie/s1001";
        String iri2 = "http://id.insee.fr/operations/serie/s1002";
        givenGroupResponse(new Ddi4GroupResponse(null, null, List.of(group(null, List.of(iri1, iri2))), null));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri1, iri2)))
                .thenReturn(Map.of(iri1, List.of("stamp-A", "stamp-B"), iri2, List.of("stamp-B", "stamp-C")));

        List<String> result = creatorsStampsOfGroup();

        assertThat(result).containsExactlyInAnyOrder("stamp-A", "stamp-B", "stamp-C");
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_returns_empty_list_when_group_response_has_null_group_list()
            throws StampFetchException, UnsupportedModuleException {
        givenGroupResponse(new Ddi4GroupResponse(null, null, null, null));

        List<String> result = creatorsStampsOfGroup();

        assertThat(result).isEmpty();
    }

    @Test
    void get_contributors_stamps_reports_a_fetch_failure_when_the_id_cannot_be_injected() throws Exception {
        // Un identifiant qui ne peut pas être sérialisé en IRI fait échouer la construction de la
        // requête : c'est un échec de récupération du timbre, pas un plantage du contrôle d'accès.
        IRI structureIri = VF.createIRI("http://bauhaus/structures/structure/1234");
        when(contributorsQueries.getContributors(GraphsPropertiesStub.stub().structuresGraph(), structureIri))
                .thenThrow(new IllegalArgumentException("Character forbidden in an IRI"));

        assertThatThrownBy(() -> checker.getContributorsStamps(RBAC.Module.STRUCTURE_STRUCTURE, "1234"))
                .isInstanceOf(StampFetchException.class);
    }

    @Test
    void get_contributors_stamps_of_a_structure_reads_its_dc_contributor_in_the_structures_graph()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        IRI structureIri = VF.createIRI("http://bauhaus/structures/structure/1234");
        givenContributorsOf(GraphsPropertiesStub.stub().structuresGraph(), structureIri.toString(), "DG75-L201");

        assertThat(checker.getContributorsStamps(RBAC.Module.STRUCTURE_STRUCTURE, "1234"))
                .containsExactly("DG75-L201");
    }

    @Test
    void get_contributors_stamps_of_a_component_reads_its_dc_contributor_in_the_components_graph()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        IRI componentIri = VF.createIRI("http://bauhaus/structures/attribut/a1");
        when(structureQueries.getComponentType("a1")).thenReturn("SELECT-type");
        when(repositoryGestion.getResponseAsObject("SELECT-type"))
                .thenReturn(new JSONObject().put("type", "http://purl.org/linked-data/cube#AttributeProperty"));
        givenContributorsOf(
                GraphsPropertiesStub.stub().structuresComponentsGraph(), componentIri.toString(), "DG75-L201");

        assertThat(checker.getContributorsStamps(RBAC.Module.STRUCTURE_COMPONENT, "a1"))
                .containsExactly("DG75-L201");
    }

    @Test
    void get_contributors_stamps_of_a_dataset_reads_the_dc_contributor_of_its_catalog_record()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        givenContributorsOf(DATASETS_GRAPH, CATALOG_RECORD_BASE_URI + "/jd1000", "DG75-L201");

        assertThat(checker.getContributorsStamps(RBAC.Module.DATASET_DATASET, "jd1000"))
                .containsExactly("DG75-L201");
    }

    @Test
    void get_contributors_stamps_of_a_concept_reads_its_dc_contributor_in_the_concepts_graph()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        IRI conceptIri = VF.createIRI("http://bauhaus/concepts/definition/c1000");
        givenContributorsOf(CONCEPTS_GRAPH, conceptIri.toString(), "DG75-L201");

        assertThat(checker.getContributorsStamps(RBAC.Module.CONCEPT_CONCEPT, "c1000"))
                .containsExactly("DG75-L201");
    }

    @Test
    void get_contributors_stamps_of_a_collection_reads_its_dc_contributor_in_the_concepts_graph()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        givenContributorsOf(CONCEPTS_GRAPH, "http://bauhaus/concepts/definitions/col1", "DG75-L201");

        assertThat(checker.getContributorsStamps(RBAC.Module.CONCEPT_COLLECTION, "col1"))
                .containsExactly("DG75-L201");
    }

    @Test
    void get_contributors_stamps_of_a_family_reads_its_dc_contributor_in_the_operations_graph()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        IRI familyIri = VF.createIRI("http://bauhaus/operations/famille/s1");
        givenContributorsOf(GraphsPropertiesStub.stub().operationsGraph(), familyIri.toString(), "DG75-L201");

        assertThat(checker.getContributorsStamps(RBAC.Module.OPERATION_FAMILY, "s1"))
                .containsExactly("DG75-L201");
    }

    @Test
    void get_contributors_stamps_of_a_classification_reads_its_dc_contributor_in_its_own_graph()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        String graph = "http://rdf.insee.fr/graphes/codes/nafr2";
        String classificationIri = "http://bauhaus/codes/nafr2/naf";
        when(classificationsQueries.getGraphUriById("nafr2")).thenReturn("SELECT-graph");
        when(repositoryGestion.getResponseAsObject("SELECT-graph"))
                .thenReturn(new JSONObject().put("graph", graph).put("uri", classificationIri));
        givenContributorsOf(graph, classificationIri, "DG75-L201");

        assertThat(checker.getContributorsStamps(RBAC.Module.CLASSIFICATION_CLASSIFICATION, "nafr2"))
                .containsExactly("DG75-L201");
    }

    @Test
    void get_contributors_stamps_of_an_unknown_classification_reports_a_fetch_failure() throws RmesException {
        when(classificationsQueries.getGraphUriById("unknown")).thenReturn("SELECT-graph");
        when(repositoryGestion.getResponseAsObject("SELECT-graph")).thenReturn(new JSONObject());

        assertThatThrownBy(() -> checker.getContributorsStamps(RBAC.Module.CLASSIFICATION_CLASSIFICATION, "unknown"))
                .isInstanceOf(StampFetchException.class);
    }

    /** La liste de codes de notation donnée a l'IRI donnée. */
    private void givenCodesList(String notation, String iri) throws RmesException {
        when(codeListsQueries.getDetailedCodeListByNotation(notation)).thenReturn("SELECT-" + notation);
        when(repositoryGestion.getResponseAsObject("SELECT-" + notation))
                .thenReturn(iri == null ? new JSONObject() : new JSONObject().put("iri", iri));
    }

    @Test
    void get_contributors_stamps_of_a_codes_list_reads_the_dc_contributor_of_the_iri_found_by_its_notation()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        String iri = "http://bauhaus/codes/cl-sexe";
        givenCodesList("CL_SEXE", iri);
        givenContributorsOf(GraphsPropertiesStub.stub().codeListGraph(), iri, "DG75-L201");

        assertThat(checker.getContributorsStamps(RBAC.Module.CODESLIST_CODESLIST, "CL_SEXE"))
                .containsExactly("DG75-L201");
    }

    @Test
    void get_contributors_stamps_of_a_partial_codes_list_reads_the_dc_contributor_of_the_iri_found_by_its_notation()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        String iri = "http://bauhaus/codes/cl-sexe-partielle";
        givenCodesList("CL_SEXE_P", iri);
        givenContributorsOf(GraphsPropertiesStub.stub().codeListGraph(), iri, "DG75-L201");

        assertThat(checker.getContributorsStamps(RBAC.Module.CODESLIST_PARTIALCODESLIST, "CL_SEXE_P"))
                .containsExactly("DG75-L201");
    }

    @Test
    void get_contributors_stamps_of_an_unknown_codes_list_reports_a_fetch_failure() throws RmesException {
        givenCodesList("unknown", null);

        assertThatThrownBy(() -> checker.getContributorsStamps(RBAC.Module.CODESLIST_CODESLIST, "unknown"))
                .isInstanceOf(StampFetchException.class);
    }

    @Test
    void get_creators_stamps_unsupported_module_throws_unsupported_module_exception() {
        assertThatThrownBy(() -> checker.getCreatorsStamps(RBAC.Module.CONCEPT_CONCEPT, "some-id"))
                .isInstanceOf(UnsupportedModuleException.class);
    }

    /**
     * Une opération n'a pas de créateur propre : ce sont ceux de sa série parente, qui la porte en
     * {@code dcterms:hasPart}, que la requête par IRI lit pour une opération.
     */
    @Test
    void get_creators_stamps_of_an_operation_reads_the_dc_creator_of_its_parent_series()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        IRI operationIri = VF.createIRI("http://bauhaus/operations/operation/s42-op1");
        when(operationSeriesQueries.getCreatorsBySeriesUri(operationIri.toString()))
                .thenReturn("SELECT-creators-of-parent-series");
        when(repositoryGestion.getResponseAsArray("SELECT-creators-of-parent-series"))
                .thenReturn(new JSONArray().put(new JSONObject().put("creators", "DG75-G401")));

        assertThat(checker.getCreatorsStamps(RBAC.Module.OPERATION_OPERATION, "s42-op1"))
                .containsExactly("DG75-G401");
    }

    @Test
    void get_creators_stamps_of_an_indicator_reads_its_dc_creator_in_the_products_graph()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        IRI indicatorIri = VF.createIRI("http://bauhaus/produits/indicateur/p1");
        String query = "SELECT-creators-of-indicator";
        when(contributorsQueries.getCreators(GraphsPropertiesStub.stub().productsGraph(), indicatorIri))
                .thenReturn(query);
        when(repositoryGestion.getResponseAsArray(query))
                .thenReturn(new JSONArray().put(new JSONObject().put("contributor", "DG75-G401")));

        assertThat(checker.getCreatorsStamps(RBAC.Module.OPERATION_INDICATOR, "p1"))
                .containsExactly("DG75-G401");
    }

    @Test
    void get_creators_stamps_operation_series_normalizes_uris_to_stamp_codes()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        String orgIri1 = "http://id.insee.fr/organisations/insee/DG75-G401";
        String orgIri2 = "http://id.insee.fr/organisations/insee/DG75-G450";
        givenSeriesCreators(orgIri1, orgIri2);
        when(organisationRepository.getOrganisationsMap(anyList()))
                .thenReturn(Map.of(
                        orgIri1, new OrganisationOption("DG75-G401", "Label 1"),
                        orgIri2, new OrganisationOption("DG75-G450", "Label 2")));

        List<String> result = creatorsStampsOfSeries();

        assertThat(result).containsExactlyInAnyOrder("DG75-G401", "DG75-G450");
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_normalizes_uris_to_stamp_codes()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        String iri1 = "http://id.insee.fr/operations/serie/s1001";
        String orgIri = "http://id.insee.fr/organisations/insee/DG75-G401";
        givenGroupResponse(new Ddi4GroupResponse(null, null, List.of(group(null, List.of(iri1))), null));
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri1))).thenReturn(Map.of(iri1, List.of(orgIri)));
        when(organisationRepository.getOrganisationsMap(anyList()))
                .thenReturn(Map.of(orgIri, new OrganisationOption("DG75-G401", "Label")));

        List<String> result = creatorsStampsOfGroup();

        assertThat(result).containsExactly("DG75-G401");
    }

    @Test
    void get_creators_stamps_operation_series_keeps_unresolved_values_as_is()
            throws StampFetchException, UnsupportedModuleException, RmesException {
        String orgIri = "http://id.insee.fr/organisations/insee/DG75-G401";
        String unresolved = "DG75-UNKNOWN";
        givenSeriesCreators(orgIri, unresolved);
        when(organisationRepository.getOrganisationsMap(anyList()))
                .thenReturn(Map.of(orgIri, new OrganisationOption("DG75-G401", "Label")));

        List<String> result = creatorsStampsOfSeries();

        assertThat(result).containsExactlyInAnyOrder("DG75-G401", "DG75-UNKNOWN");
    }
}
