package fr.insee.rmes.modules.ddi.operations_mirror.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.modules.operation.domain.event.BilingualLabel;
import fr.insee.rmes.modules.operation.domain.event.OperationSaved;
import fr.insee.rmes.modules.operation.domain.event.SeriesSaved;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class DomainOperationsMirrorServiceTest {

    private static final String SERIES_IRI = "http://id.insee.fr/operations/serie/s1001";
    private static final String AGENCY = "fr.insee";

    private final GroupService groupService = mock(GroupService.class);
    private final StudyUnitService studyUnitService = mock(StudyUnitService.class);
    private final DDIService ddiService = mock(DDIService.class);

    private final DomainOperationsMirrorService service = new DomainOperationsMirrorService(
            groupService, studyUnitService, ddiService, AGENCY, List.of("fr-FR", "en-GB"));

    @Test
    void mirrorSeries_createsAGroupIdentifiedByTheSeriesIri() {
        when(groupService.find(AGENCY, expectedGroupId())).thenReturn(Optional.empty());

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Recensement", "Census"), new BilingualLabel("RP", "CENS")));

        Ddi4Group group = capturedGroup();
        assertThat(group.id()).isEqualTo(expectedGroupId());
        assertThat(group.agency()).isEqualTo(AGENCY);
        assertThat(group.version()).isEqualTo("1");
        assertThat(group.seriesIris()).containsExactly(SERIES_IRI);
        assertThat(group.typeOfGroup()).isEqualTo("insee:StatisticalOperationSeries");
    }

    @Test
    void mirrorSeries_writesThePrefLabelAsTitleAndTheAltLabelAsAlternateTitle() {
        when(groupService.find(AGENCY, expectedGroupId())).thenReturn(Optional.empty());

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Recensement", "Census"), new BilingualLabel("RP", "CENS")));

        Citation citation = capturedGroup().citation();
        assertThat(citation.title())
                .containsExactly(new LangString("fr-FR", "Recensement"), new LangString("en-GB", "Census"));
        assertThat(citation.alternateTitle())
                .containsExactly(new LangString("fr-FR", "RP"), new LangString("en-GB", "CENS"));
    }

    @Test
    void mirrorSeries_omitsTheLabelsThatTheSeriesDoesNotCarry() {
        when(groupService.find(AGENCY, expectedGroupId())).thenReturn(Optional.empty());

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Recensement", null), new BilingualLabel(null, null)));

        Citation citation = capturedGroup().citation();
        assertThat(citation.title()).containsExactly(new LangString("fr-FR", "Recensement"));
        assertThat(citation.alternateTitle()).isNull();
    }

    @Test
    void mirrorSeries_keepsTheStudyUnitsAlreadyFiledUnderTheGroup() {
        Reference studyUnit = Reference.of(AGENCY, "su-1", "1", "StudyUnit");
        Reference logicalProduct = Reference.of(AGENCY, "lp-1", "1", "LogicalProduct");
        when(groupService.find(AGENCY, expectedGroupId()))
                .thenReturn(Optional.of(new Ddi4Group(
                        Ddi4Group.TYPE,
                        null,
                        null,
                        AGENCY,
                        expectedGroupId(),
                        "1",
                        null,
                        new Citation(List.of(new LangString("fr-FR", "Ancien libellé"))),
                        List.of(studyUnit),
                        List.of(SERIES_IRI),
                        "insee:StatisticalOperationSeries",
                        List.of(logicalProduct))));

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Nouveau libellé", null), new BilingualLabel(null, null)));

        Ddi4Group group = capturedGroup();
        assertThat(group.studyUnitReference()).containsExactly(studyUnit);
        assertThat(group.logicalProductReference()).containsExactly(logicalProduct);
        assertThat(group.citation().title()).containsExactly(new LangString("fr-FR", "Nouveau libellé"));
    }

    @Test
    void mirrorSeries_keepsTheOtherSeriesIrisAlreadyCarriedByTheGroup() {
        String otherSeriesIri = "http://id.insee.fr/operations/serie/s2002";
        when(groupService.find(AGENCY, expectedGroupId()))
                .thenReturn(Optional.of(new Ddi4Group(
                        Ddi4Group.TYPE,
                        null,
                        null,
                        AGENCY,
                        expectedGroupId(),
                        "1",
                        null,
                        new Citation(List.of(new LangString("fr-FR", "Ancien libellé"))),
                        null,
                        List.of(otherSeriesIri, SERIES_IRI),
                        "insee:StatisticalOperationSeries")));

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Nouveau libellé", null), new BilingualLabel(null, null)));

        assertThat(capturedGroup().seriesIris())
                .as("l'IRI de chaque série du groupe est la clé par laquelle ses stamps sont résolus")
                .containsExactly(otherSeriesIri, SERIES_IRI);
    }

    @Test
    void mirrorSeries_reusesTheVersionOfTheExistingGroupSoThatColecticaDoesNotBumpIt() {
        when(groupService.find(AGENCY, expectedGroupId()))
                .thenReturn(Optional.of(new Ddi4Group(
                        Ddi4Group.TYPE,
                        null,
                        null,
                        AGENCY,
                        expectedGroupId(),
                        "3",
                        null,
                        new Citation(List.of(new LangString("fr-FR", "Ancien libellé"))),
                        null,
                        List.of(SERIES_IRI),
                        "insee:StatisticalOperationSeries")));

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Nouveau libellé", null), new BilingualLabel(null, null)));

        assertThat(capturedGroup().version()).isEqualTo("3");
        verify(studyUnitService, never()).createOrUpdate(any());
    }

    private Ddi4Group capturedGroup() {
        ArgumentCaptor<Ddi4Group> captor = ArgumentCaptor.forClass(Ddi4Group.class);
        verify(groupService).createOrUpdate(captor.capture());
        return captor.getValue();
    }

    private static String expectedGroupId() {
        return UUID.nameUUIDFromBytes(SERIES_IRI.getBytes(StandardCharsets.UTF_8))
                .toString();
    }

    // --- Opérations -----------------------------------------------------------------------------

    private static final String OPERATION_IRI = "http://id.insee.fr/operations/operation/o1500";

    private OperationSaved operationSaved(String seriesIri) {
        return new OperationSaved(
                OPERATION_IRI,
                "o1500",
                seriesIri,
                new BilingualLabel("Enquête emploi", "Labour force survey"),
                new BilingualLabel("EEC", "LFS"));
    }

    @Test
    void mirrorOperation_createsTheVariableSchemeThenItsLogicalProductThenTheStudyUnit() {
        when(groupService.find(AGENCY, expectedGroupId())).thenReturn(Optional.empty());
        when(studyUnitService.find(AGENCY, expectedStudyUnitId())).thenReturn(Optional.empty());

        service.mirror(operationSaved(SERIES_IRI));

        ArgumentCaptor<Ddi4VariableScheme> variableScheme = ArgumentCaptor.forClass(Ddi4VariableScheme.class);
        ArgumentCaptor<Ddi4LogicalProduct> logicalProduct = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        ArgumentCaptor<Ddi4StudyUnit> studyUnit = ArgumentCaptor.forClass(Ddi4StudyUnit.class);
        InOrder inOrder = inOrder(ddiService, studyUnitService);
        inOrder.verify(ddiService).createVariableScheme(variableScheme.capture());
        inOrder.verify(ddiService).createLogicalProduct(logicalProduct.capture());
        inOrder.verify(studyUnitService).createOrUpdate(studyUnit.capture());

        assertThat(variableScheme.getValue().name()).isEqualTo("VS-EEC");
        assertThat(variableScheme.getValue().label())
                .containsExactly(new LangString("fr-FR", "Ensemble de variables EEC"));
        assertThat(logicalProduct.getValue().name()).isEqualTo("LP-EEC");
        assertThat(logicalProduct.getValue().label()).containsExactly(new LangString("fr-FR", "EEC"));
        assertThat(logicalProduct.getValue().variableSchemeReference())
                .containsExactly(Reference.of(AGENCY, variableScheme.getValue().id(), "1", "VariableScheme"));
        assertThat(studyUnit.getValue().id()).isEqualTo(expectedStudyUnitId());
        assertThat(studyUnit.getValue().operationIri()).isEqualTo(OPERATION_IRI);
        assertThat(studyUnit.getValue().citation().title())
                .containsExactly(
                        new LangString("fr-FR", "Enquête emploi"), new LangString("en-GB", "Labour force survey"));
        assertThat(studyUnit.getValue().logicalProductReferences())
                .containsExactly(Reference.of(AGENCY, logicalProduct.getValue().id(), "1", "LogicalProduct"));
    }

    @Test
    void mirrorOperation_filesTheStudyUnitUnderTheGroupOfItsSeries() {
        when(groupService.find(AGENCY, expectedGroupId())).thenReturn(Optional.of(existingGroup(null)));
        when(studyUnitService.find(AGENCY, expectedStudyUnitId())).thenReturn(Optional.empty());

        service.mirror(operationSaved(SERIES_IRI));

        assertThat(capturedGroup().studyUnitReference())
                .containsExactly(Reference.of(AGENCY, expectedStudyUnitId(), "1", "StudyUnit"));
    }

    @Test
    void mirrorOperation_doesNotFileTheStudyUnitTwiceUnderItsGroup() {
        Reference alreadyFiled = Reference.of(AGENCY, expectedStudyUnitId(), "1", "StudyUnit");
        when(groupService.find(AGENCY, expectedGroupId()))
                .thenReturn(Optional.of(existingGroup(List.of(alreadyFiled))));
        when(studyUnitService.find(AGENCY, expectedStudyUnitId())).thenReturn(Optional.empty());

        service.mirror(operationSaved(SERIES_IRI));

        verify(groupService, never()).createOrUpdate(any());
    }

    @Test
    void mirrorOperation_keepsThePhysicalInstancesAlreadyLinkedToTheStudyUnit() {
        Reference physicalInstance = Reference.of(AGENCY, "pi-1", "1", "PhysicalInstance");
        when(studyUnitService.find(AGENCY, expectedStudyUnitId()))
                .thenReturn(Optional.of(new Ddi4StudyUnit(
                        Ddi4StudyUnit.TYPE,
                        null,
                        null,
                        AGENCY,
                        expectedStudyUnitId(),
                        "2",
                        new Citation(List.of(new LangString("fr-FR", "Ancien libellé"))),
                        OPERATION_IRI,
                        List.of(physicalInstance))));

        service.mirror(operationSaved(null));

        ArgumentCaptor<Ddi4StudyUnit> captor = ArgumentCaptor.forClass(Ddi4StudyUnit.class);
        verify(studyUnitService).createOrUpdate(captor.capture());
        assertThat(captor.getValue().physicalInstanceReferences()).containsExactly(physicalInstance);
        assertThat(captor.getValue().version()).isEqualTo("2");
    }

    @Test
    void mirrorOperation_leavesEveryGroupAloneWhenTheEventDoesNotNameTheSeries() {
        when(studyUnitService.find(AGENCY, expectedStudyUnitId())).thenReturn(Optional.empty());

        service.mirror(operationSaved(null));

        verify(groupService, never()).find(any(), any());
        verify(groupService, never()).createOrUpdate(any());
    }

    private Ddi4Group existingGroup(List<Reference> studyUnitReferences) {
        return new Ddi4Group(
                Ddi4Group.TYPE,
                null,
                null,
                AGENCY,
                expectedGroupId(),
                "1",
                null,
                new Citation(List.of(new LangString("fr-FR", "Recensement"))),
                studyUnitReferences,
                List.of(SERIES_IRI),
                "insee:StatisticalOperationSeries");
    }

    private static String expectedStudyUnitId() {
        return UUID.nameUUIDFromBytes(OPERATION_IRI.getBytes(StandardCharsets.UTF_8))
                .toString();
    }
}
