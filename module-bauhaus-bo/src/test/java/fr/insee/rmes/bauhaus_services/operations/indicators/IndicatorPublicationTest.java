package fr.insee.rmes.bauhaus_services.operations.indicators;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import java.util.Optional;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IndicatorPublicationTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final String GESTION = "http://gestion/";
    private static final String PUBLICATION = "http://publication/";

    @Mock
    private OperationsParentRepository operationsParentRepository;

    @Mock
    private RepositoryGestion repoGestion;

    @Mock
    private RepositoryPublication repositoryPublication;

    @Spy
    private PublicationUtils publicationUtils = new PublicationUtils(GESTION, PUBLICATION, null, null);

    @InjectMocks
    private IndicatorPublication indicatorPublication;

    private Indicator indicator;

    @BeforeEach
    void setUp() throws RmesException {
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder(PUBLICATION, GESTION, name -> Optional.of("operations/indicateur")));
        indicator = new Indicator();
        indicator.setId("123");
    }

    @Test
    void validate_ShouldThrowBadRequestException_WhenIndicatorIsAlreadyPublished() throws RmesException {
        OperationsLink link = new OperationsLink();
        link.id = "series-1";
        indicator.wasGeneratedBy = List.of(link);

        when(operationsParentRepository.getIndicatorsValidationStatus("123"))
                .thenReturn(ValidationStatus.VALIDATED.getValue());

        RmesBadRequestException exception =
                assertThrows(RmesBadRequestException.class, () -> indicatorPublication.validate(indicator));
        assertThat(exception.getDetails()).contains("\"code\":1301");
        assertThat(exception.getDetails()).contains("Indicator: 123");
    }

    @Test
    void validate_ShouldThrowBadRequestException_WhenParentSeriesIsNotValidated() throws RmesException {
        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            givenIndicatorGeneratedBySeriesWithStatus(mockedFactory, ValidationStatus.UNPUBLISHED.toString());

            RmesBadRequestException exception =
                    assertThrows(RmesBadRequestException.class, () -> indicatorPublication.validate(indicator));
            assertThat(exception.getDetails())
                    .contains("An indicator can be published if and only if all parent series have been published.");
        }
    }

    @Test
    void validate_ShouldPass_WhenUserHasPermissionAndParentSeriesAreValidated() throws RmesException {
        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            givenIndicatorGeneratedBySeriesWithStatus(mockedFactory, ValidationStatus.VALIDATED.toString());

            assertDoesNotThrow(() -> indicatorPublication.validate(indicator));
        }
    }

    @Test
    void validate_ShouldThrowBadRequestException_WhenIndicatorIsNotLinkedToAnySeries() throws RmesException {
        indicator.wasGeneratedBy = List.of();
        when(operationsParentRepository.getIndicatorsValidationStatus("123"))
                .thenReturn(ValidationStatus.UNPUBLISHED.getValue());

        RmesBadRequestException exception =
                assertThrows(RmesBadRequestException.class, () -> indicatorPublication.validate(indicator));

        assertThat(exception.getDetails()).contains("An indicator should be linked to a series.");
    }

    @Test
    void publish_ShouldRejectAnIndicatorThatDoesNotExist() throws RmesException {
        givenStatementsOfIndicator();

        RmesException exception = assertThrows(RmesException.class, () -> indicatorPublication.publish("123"));

        assertThat(exception.getStatus()).isEqualTo(404);
    }

    /**
     * Les liens de l'indicateur sont réécrits vers la base de publication, ses littéraux recopiés
     * tels quels, et son état de validation reste en gestion.
     */
    @Test
    void publish_ShouldRewriteTheLinksAndDropTheValidationState() throws RmesException {
        Resource indicatorIri = RdfUtils.objectIRI(ObjectType.INDICATOR, "123");
        givenStatementsOfIndicator(
                VF.createStatement(
                        (org.eclipse.rdf4j.model.IRI) indicatorIri,
                        VF.createIRI("http://rdf.insee.fr/def/base#validationState"),
                        VF.createLiteral("Validated")),
                VF.createStatement(
                        (org.eclipse.rdf4j.model.IRI) indicatorIri,
                        VF.createIRI("http://www.w3.org/ns/prov#wasGeneratedBy"),
                        VF.createIRI(GESTION + "operations/serie/s1000")),
                VF.createStatement(
                        (org.eclipse.rdf4j.model.IRI) indicatorIri,
                        SKOS.PREF_LABEL,
                        VF.createLiteral("label fr", "fr")));

        indicatorPublication.publish("123");

        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication).publishResource(any(Resource.class), modelCaptor.capture(), eq("indicator"));
        Model model = modelCaptor.getValue();
        assertThat(model.size()).isEqualTo(2);
        assertThat(model.contains(
                        null,
                        VF.createIRI("http://www.w3.org/ns/prov#wasGeneratedBy"),
                        VF.createIRI(PUBLICATION + "operations/serie/s1000")))
                .isTrue();
        assertThat(model.contains(null, SKOS.PREF_LABEL, VF.createLiteral("label fr", "fr")))
                .isTrue();
    }

    private void givenIndicatorGeneratedBySeriesWithStatus(MockedStatic<RdfUtils> mockedFactory, String seriesStatus)
            throws RmesException {
        OperationsLink link = new OperationsLink();
        link.id = "series-1";
        when(operationsParentRepository.getValidationStatus("series-1")).thenReturn(seriesStatus);
        indicator.wasGeneratedBy = List.of(link);

        mockedFactory
                .when(() -> RdfUtils.objectIRI(eq(ObjectType.INDICATOR), eq("123")))
                .thenReturn(SimpleValueFactory.getInstance().createIRI("http://indicator/1"));
    }

    private void givenStatementsOfIndicator(Statement... statements) throws RmesException {
        when(repoGestion.getConnection()).thenReturn(null);
        when(repoGestion.getStatements(any(), any(Resource.class)))
                .thenReturn(new RepositoryResult<>(
                        new CloseableIteratorIteration<>(List.of(statements).iterator())));
    }
}
