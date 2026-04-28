package fr.insee.rmes.modules.users.infrastructure.stamps;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.datasets.datasets.infrastructure.DatasetQueries;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.modules.users.domain.exceptions.StampFetchException;
import fr.insee.rmes.modules.users.domain.exceptions.UnsupportedModuleException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetDistributionQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphDbStampCheckerTest {

    @Mock
    private RepositoryGestion repositoryGestion;
    @Mock
    private DatasetQueries datasetQueries;
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

    private GraphDbStampChecker checker;

    @BeforeEach
    void setUp() {
        checker = new GraphDbStampChecker(repositoryGestion, datasetQueries, structureQueries,
                operationSeriesQueries, ddiRepository, seriesCreatorsPort, datasetDistributionQueries);
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_returns_empty_list_when_id_is_null() throws StampFetchException, UnsupportedModuleException {
        List<String> result = checker.getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, null);

        assertThat(result).isEmpty();
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_returns_empty_list_when_id_has_no_delimiter() throws StampFetchException, UnsupportedModuleException {
        List<String> result = checker.getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, "group-id-only");

        assertThat(result).isEmpty();
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_returns_empty_list_when_group_has_no_series() throws StampFetchException, UnsupportedModuleException {
        // studyUnitReference=List.of(), seriesIris=List.of()
        Ddi4Group group = new Ddi4Group(null, null, null, "fr.insee", "group-id", "1",
                null, null, List.of(), List.of(), null);
        Ddi4GroupResponse groupResponse = new Ddi4GroupResponse(null, null, List.of(group), null);
        when(ddiRepository.getGroup("fr.insee", "group-id")).thenReturn(groupResponse);

        List<String> result = checker.getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, "fr.insee|group-id");

        assertThat(result).isEmpty();
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_returns_creators_when_series_have_creators() throws StampFetchException, UnsupportedModuleException {
        String iri1 = "http://id.insee.fr/operations/serie/s1001";
        String iri2 = "http://id.insee.fr/operations/serie/s1002";
        // studyUnitReference=null, seriesIris=List.of(iri1, iri2)
        Ddi4Group group = new Ddi4Group(null, null, null, "fr.insee", "group-id", "1",
                null, null, null, List.of(iri1, iri2), null);
        Ddi4GroupResponse groupResponse = new Ddi4GroupResponse(null, null, List.of(group), null);
        when(ddiRepository.getGroup("fr.insee", "group-id")).thenReturn(groupResponse);
        when(seriesCreatorsPort.getCreatorsForSeries(List.of(iri1, iri2)))
                .thenReturn(Map.of(iri1, List.of("stamp-A", "stamp-B"), iri2, List.of("stamp-B", "stamp-C")));

        List<String> result = checker.getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, "fr.insee|group-id");

        assertThat(result).containsExactlyInAnyOrder("stamp-A", "stamp-B", "stamp-C");
    }

    @Test
    void get_creators_stamps_ddi_physical_instance_returns_empty_list_when_group_response_has_null_group_list() throws StampFetchException, UnsupportedModuleException {
        Ddi4GroupResponse groupResponse = new Ddi4GroupResponse(null, null, null, null);
        when(ddiRepository.getGroup("fr.insee", "group-id")).thenReturn(groupResponse);

        List<String> result = checker.getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, "fr.insee|group-id");

        assertThat(result).isEmpty();
    }

    @Test
    void get_creators_stamps_unsupported_module_throws_unsupported_module_exception() {
        assertThatThrownBy(() -> checker.getCreatorsStamps(RBAC.Module.CONCEPT_CONCEPT, "some-id"))
                .isInstanceOf(UnsupportedModuleException.class);
    }
}
