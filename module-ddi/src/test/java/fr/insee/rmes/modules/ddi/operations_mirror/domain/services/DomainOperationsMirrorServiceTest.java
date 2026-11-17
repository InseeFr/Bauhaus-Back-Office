package fr.insee.rmes.modules.ddi.operations_mirror.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
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
        mirrorCensusSeriesWithoutExistingGroup();

        Ddi4Group group = capturedGroup();
        assertThat(group.id()).isEqualTo(expectedGroupId());
        assertThat(group.agency()).isEqualTo(AGENCY);
        assertThat(group.version()).isEqualTo("1");
        assertThat(group.seriesIris()).containsExactly(SERIES_IRI);
        assertThat(group.typeOfGroup()).isEqualTo("insee:StatisticalOperationSeries");
    }

    @Test
    void mirrorSeries_writesThePrefLabelAsTitleAndTheAltLabelAsAlternateTitle() {
        mirrorCensusSeriesWithoutExistingGroup();

        Citation citation = capturedGroup().citation();
        assertThat(citation.title())
                .containsExactly(new LangString("fr-FR", "Recensement"), new LangString("en-GB", "Census"));
        assertThat(citation.alternateTitle())
                .containsExactly(new LangString("fr-FR", "RP"), new LangString("en-GB", "CENS"));
    }

    private void mirrorCensusSeriesWithoutExistingGroup() {
        when(groupService.find(AGENCY, expectedGroupId())).thenReturn(Optional.empty());

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Recensement", "Census"), new BilingualLabel("RP", "CENS")));
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
                .thenReturn(Optional.of(existingGroup(
                        "1", "Ancien libellé", List.of(studyUnit), List.of(SERIES_IRI), List.of(logicalProduct))));

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
                .thenReturn(Optional.of(
                        existingGroup("1", "Ancien libellé", null, List.of(otherSeriesIri, SERIES_IRI), null)));

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Nouveau libellé", null), new BilingualLabel(null, null)));

        assertThat(capturedGroup().seriesIris())
                .as("l'IRI de chaque série du groupe est la clé par laquelle ses stamps sont résolus")
                .containsExactly(otherSeriesIri, SERIES_IRI);
    }

    @Test
    void mirrorSeries_reusesTheVersionOfTheExistingGroupSoThatColecticaDoesNotBumpIt() {
        when(groupService.find(AGENCY, expectedGroupId()))
                .thenReturn(Optional.of(existingGroup("3", "Ancien libellé", null, List.of(SERIES_IRI), null)));

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Nouveau libellé", null), new BilingualLabel(null, null)));

        assertThat(capturedGroup().version()).isEqualTo("3");
        verify(studyUnitService, never()).createOrUpdate(any());
    }

    @Test
    void mirrorSeries_createsTheSchemesThenTheirLogicalProductThenTheGroup() {
        mirrorCensusSeriesWithoutExistingGroup();

        ArgumentCaptor<Ddi4CodeListScheme> codeListScheme = ArgumentCaptor.forClass(Ddi4CodeListScheme.class);
        ArgumentCaptor<Ddi4CategoryScheme> categoryScheme = ArgumentCaptor.forClass(Ddi4CategoryScheme.class);
        ArgumentCaptor<Ddi4ManagedRepresentationScheme> managedRepresentationScheme =
                ArgumentCaptor.forClass(Ddi4ManagedRepresentationScheme.class);
        ArgumentCaptor<Ddi4LogicalProduct> logicalProduct = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        ArgumentCaptor<Ddi4Group> group = ArgumentCaptor.forClass(Ddi4Group.class);
        InOrder inOrder = inOrder(ddiService, groupService);
        inOrder.verify(ddiService).createCodeListScheme(codeListScheme.capture());
        inOrder.verify(ddiService).createCategoryScheme(categoryScheme.capture());
        inOrder.verify(ddiService).createManagedRepresentationScheme(managedRepresentationScheme.capture());
        inOrder.verify(ddiService).createLogicalProduct(logicalProduct.capture());
        inOrder.verify(groupService).createOrUpdate(group.capture());

        assertThat(logicalProduct.getValue().name()).isEqualTo("LP-RP");
        assertThat(logicalProduct.getValue().label()).containsExactly(new LangString("fr-FR", "RP"));
        assertThat(logicalProduct.getValue().codeListSchemeReference())
                .containsExactly(Reference.of(AGENCY, codeListScheme.getValue().id(), "1", "CodeListScheme"));
        assertThat(logicalProduct.getValue().categorySchemeReference())
                .containsExactly(Reference.of(AGENCY, categoryScheme.getValue().id(), "1", "CategoryScheme"));
        assertThat(logicalProduct.getValue().managedRepresentationSchemeReference())
                .containsExactly(Reference.of(
                        AGENCY, managedRepresentationScheme.getValue().id(), "1", "ManagedRepresentationScheme"));
        assertThat(logicalProduct.getValue().variableSchemeReference())
                .as("la VariableScheme vit dans le LogicalProduct de la StudyUnit, jamais dans celui du Group")
                .isNull();
        assertThat(group.getValue().logicalProductReference())
                .containsExactly(Reference.of(AGENCY, logicalProduct.getValue().id(), "1", "LogicalProduct"));
    }

    @Test
    void mirrorSeries_givesALogicalProductToAnExistingGroupThatHasNone() {
        when(groupService.find(AGENCY, expectedGroupId()))
                .thenReturn(Optional.of(existingGroup("1", "Ancien libellé", null, List.of(SERIES_IRI), null)));

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Nouveau libellé", null), new BilingualLabel(null, null)));

        ArgumentCaptor<Ddi4LogicalProduct> logicalProduct = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        verify(ddiService).createLogicalProduct(logicalProduct.capture());
        assertThat(logicalProduct.getValue().name())
                .as("sans libellé court, le libellé long en tient lieu")
                .isEqualTo("LP-NOUVEAULIBELLE");
        assertThat(capturedGroup().logicalProductReference())
                .containsExactly(Reference.of(AGENCY, logicalProduct.getValue().id(), "1", "LogicalProduct"));
    }

    @Test
    void mirrorSeries_leavesTheLogicalProductOfTheGroupAloneWhenItAlreadyHasOne() {
        Reference logicalProduct = Reference.of(AGENCY, "lp-1", "1", "LogicalProduct");
        when(groupService.find(AGENCY, expectedGroupId()))
                .thenReturn(Optional.of(
                        existingGroup("1", "Ancien libellé", null, List.of(SERIES_IRI), List.of(logicalProduct))));

        service.mirror(new SeriesSaved(
                SERIES_IRI, "s1001", new BilingualLabel("Nouveau libellé", null), new BilingualLabel(null, null)));

        verify(ddiService, never()).createLogicalProduct(any());
        verify(ddiService, never()).createCodeListScheme(any());
        verify(ddiService, never()).createCategoryScheme(any());
        verify(ddiService, never()).createManagedRepresentationScheme(any());
        assertThat(capturedGroup().logicalProductReference()).containsExactly(logicalProduct);
    }

    // --- Renommage d'une série : LogicalProduct et schemes du Group (#1152) ----------------------

    private static final Reference GROUP_LOGICAL_PRODUCT = Reference.of(AGENCY, "lp-1", "2", "LogicalProduct");
    private static final Reference CODE_LIST_SCHEME = Reference.of(AGENCY, "cls-1", "1", "CodeListScheme");
    private static final Reference CATEGORY_SCHEME = Reference.of(AGENCY, "cas-1", "3", "CategoryScheme");
    private static final Reference MANAGED_REPRESENTATION_SCHEME =
            Reference.of(AGENCY, "mrs-1", "1", "ManagedRepresentationScheme");
    private static final Reference CODE_LIST = Reference.of(AGENCY, "cl-1", "1", "CodeList");
    private static final Reference CATEGORY = Reference.of(AGENCY, "ca-1", "1", "Category");
    private static final Reference MANAGED_REPRESENTATION =
            Reference.of(AGENCY, "mmvr-1", "1", "ManagedMissingValuesRepresentation");

    private void givenAGroupWhoseLogicalProductIsLabelled(String shortLabel) {
        when(groupService.find(AGENCY, expectedGroupId()))
                .thenReturn(Optional.of(
                        existingGroup("1", "Recensement", null, List.of(SERIES_IRI), List.of(GROUP_LOGICAL_PRODUCT))));
        when(ddiService.findLogicalProduct(AGENCY, "lp-1"))
                .thenReturn(Optional.of(new Ddi4LogicalProduct(
                        Ddi4LogicalProduct.TYPE,
                        null,
                        "urn:ddi:fr.insee:lp-1:2",
                        AGENCY,
                        "lp-1",
                        "2",
                        List.of(new LangString("fr-FR", shortLabel), new LangString("en-GB", "Census")),
                        List.of(CODE_LIST_SCHEME),
                        List.of(CATEGORY_SCHEME),
                        null,
                        List.of(MANAGED_REPRESENTATION_SCHEME),
                        "LP-" + shortLabel)));
        when(ddiService.findCodeListScheme(AGENCY, "cls-1"))
                .thenReturn(Optional.of(new Ddi4CodeListScheme(
                        Ddi4CodeListScheme.TYPE,
                        null,
                        "urn:ddi:fr.insee:cls-1:1",
                        AGENCY,
                        "cls-1",
                        "1",
                        List.of(new LangString("fr-FR", "Ensemble de listes de codes " + shortLabel)),
                        List.of(CODE_LIST))));
        when(ddiService.findCategoryScheme(AGENCY, "cas-1"))
                .thenReturn(Optional.of(new Ddi4CategoryScheme(
                        Ddi4CategoryScheme.TYPE,
                        null,
                        "urn:ddi:fr.insee:cas-1:3",
                        AGENCY,
                        "cas-1",
                        "3",
                        List.of(new LangString("fr-FR", "Ensemble de catégories " + shortLabel)),
                        List.of(CATEGORY))));
        when(ddiService.findManagedRepresentationScheme(AGENCY, "mrs-1"))
                .thenReturn(Optional.of(new Ddi4ManagedRepresentationScheme(
                        Ddi4ManagedRepresentationScheme.TYPE,
                        null,
                        "urn:ddi:fr.insee:mrs-1:1",
                        AGENCY,
                        "mrs-1",
                        "1",
                        List.of(new LangString("fr-FR", "Ensemble de représentations " + shortLabel)),
                        List.of(MANAGED_REPRESENTATION))));
    }

    private void renameCensusSeriesTo(String shortLabel) {
        service.mirror(new SeriesSaved(
                SERIES_IRI,
                "s1001",
                new BilingualLabel("Recensement", "Census"),
                new BilingualLabel(shortLabel, null)));
    }

    @Test
    void mirrorSeries_relabelsTheLogicalProductOfTheGroupWithoutBumpingItsVersion() {
        givenAGroupWhoseLogicalProductIsLabelled("RP");

        renameCensusSeriesTo("RPX");

        ArgumentCaptor<Ddi4LogicalProduct> captor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        verify(ddiService).createLogicalProduct(captor.capture());
        Ddi4LogicalProduct logicalProduct = captor.getValue();
        assertThat(logicalProduct.id()).isEqualTo("lp-1");
        assertThat(logicalProduct.version()).isEqualTo("2");
        assertThat(logicalProduct.urn()).isEqualTo("urn:ddi:fr.insee:lp-1:2");
        assertThat(logicalProduct.name()).isEqualTo("LP-RPX");
        assertThat(logicalProduct.label())
                .as("seul le libellé de la langue principale suit le nom court de la série")
                .containsExactly(new LangString("fr-FR", "RPX"), new LangString("en-GB", "Census"));
        assertThat(logicalProduct.codeListSchemeReference()).containsExactly(CODE_LIST_SCHEME);
        assertThat(logicalProduct.categorySchemeReference()).containsExactly(CATEGORY_SCHEME);
        assertThat(logicalProduct.managedRepresentationSchemeReference())
                .containsExactly(MANAGED_REPRESENTATION_SCHEME);
        assertThat(capturedGroup().logicalProductReference()).containsExactly(GROUP_LOGICAL_PRODUCT);
    }

    @Test
    void mirrorSeries_relabelsTheSchemesOfTheGroupAndKeepsWhatTheyClassify() {
        givenAGroupWhoseLogicalProductIsLabelled("RP");

        renameCensusSeriesTo("RPX");

        ArgumentCaptor<Ddi4CodeListScheme> codeListScheme = ArgumentCaptor.forClass(Ddi4CodeListScheme.class);
        verify(ddiService).createCodeListScheme(codeListScheme.capture());
        assertThat(codeListScheme.getValue().version()).isEqualTo("1");
        assertThat(codeListScheme.getValue().label())
                .containsExactly(new LangString("fr-FR", "Ensemble de listes de codes RPX"));
        assertThat(codeListScheme.getValue().codeListReference()).containsExactly(CODE_LIST);

        ArgumentCaptor<Ddi4CategoryScheme> categoryScheme = ArgumentCaptor.forClass(Ddi4CategoryScheme.class);
        verify(ddiService).createCategoryScheme(categoryScheme.capture());
        assertThat(categoryScheme.getValue().version()).isEqualTo("3");
        assertThat(categoryScheme.getValue().label())
                .containsExactly(new LangString("fr-FR", "Ensemble de catégories RPX"));
        assertThat(categoryScheme.getValue().categoryReference()).containsExactly(CATEGORY);

        ArgumentCaptor<Ddi4ManagedRepresentationScheme> managedRepresentationScheme =
                ArgumentCaptor.forClass(Ddi4ManagedRepresentationScheme.class);
        verify(ddiService).createManagedRepresentationScheme(managedRepresentationScheme.capture());
        assertThat(managedRepresentationScheme.getValue().label())
                .containsExactly(new LangString("fr-FR", "Ensemble de représentations RPX"));
        assertThat(managedRepresentationScheme.getValue().managedRepresentationReference())
                .containsExactly(MANAGED_REPRESENTATION);
    }

    @Test
    void mirrorSeries_rewritesNeitherTheLogicalProductNorItsSchemesWhenTheShortLabelIsUnchanged() {
        givenAGroupWhoseLogicalProductIsLabelled("RP");

        renameCensusSeriesTo("RP");

        verify(ddiService, never()).createLogicalProduct(any());
        verify(ddiService, never()).createCodeListScheme(any());
        verify(ddiService, never()).createCategoryScheme(any());
        verify(ddiService, never()).createManagedRepresentationScheme(any());
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
        return existingGroup("1", "Recensement", studyUnitReferences, List.of(SERIES_IRI), null);
    }

    private static Ddi4Group existingGroup(
            String version,
            String frenchTitle,
            List<Reference> studyUnitReferences,
            List<String> seriesIris,
            List<Reference> logicalProductReferences) {
        return new Ddi4Group(
                Ddi4Group.TYPE,
                null,
                null,
                AGENCY,
                expectedGroupId(),
                version,
                null,
                new Citation(List.of(new LangString("fr-FR", frenchTitle))),
                studyUnitReferences,
                seriesIris,
                "insee:StatisticalOperationSeries",
                logicalProductReferences);
    }

    private static String expectedStudyUnitId() {
        return UUID.nameUUIDFromBytes(OPERATION_IRI.getBytes(StandardCharsets.UTF_8))
                .toString();
    }
}
