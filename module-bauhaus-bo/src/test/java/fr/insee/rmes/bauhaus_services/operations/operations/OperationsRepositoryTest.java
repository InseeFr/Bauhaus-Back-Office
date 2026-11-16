package fr.insee.rmes.bauhaus_services.operations.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.OperationsObjectMapper;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.config.DocumentationsPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.modules.operation.domain.event.BilingualLabel;
import fr.insee.rmes.modules.operation.domain.event.OperationSaved;
import fr.insee.rmes.modules.operations.operations.domain.model.commands.OperationCommand;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.modules.shared_kernel.infrastructure.publication.ObjectPublished;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OperationsRepositoryTest {
    @InjectMocks
    OperationsRepository operationsRepository;

    @Spy
    BauhausLanguagesProperties languages = new BauhausLanguagesProperties("fr", "en");

    @Mock
    OperationsObjectMapper operationsObjectMapper;

    @Mock
    OperationsParentRepository operationsParentRepository;

    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    OperationsOperationQueries operationsOperationQueries;

    @Mock
    ApplicationEventPublisher events;

    @Mock
    DocumentationsUtils documentationsUtils;

    @Mock
    OperationPublication operationPublication;

    @Spy
    BauhausUriBuilder bauhausUriBuilder =
            new BauhausUriBuilder("http://id.insee.fr", "http://bauhaus", p -> Optional.of("/operations/operation"));

    @Spy
    BauhausIriFactory iriFactory = new BauhausIriFactory(
            GraphsPropertiesStub.stub(),
            new BauhausUriBuilder("http://bauhaus/publication/", "http://bauhaus/", p -> Optional.of("/operations")),
            DocumentationsPropertiesStub.stub());

    @Test
    void createRdfOperation_addsAdmsIdentifierTriple() throws RmesException {
        when(repositoryGestion.getResponseAsBoolean(any())).thenReturn(false);
        Operation operation = Operation.of("o1500");
        operation.setPrefLabelLg1("Opération de test");

        operationsRepository.createRdfOperation(operation, null, ValidationStatus.UNPUBLISHED);

        ArgumentCaptor<Model> captor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryGestion).loadSimpleObject(any(), captor.capture());
        IRI operationURI = iriFactory.objectIri(ObjectType.OPERATION, "o1500");
        assertThat(captor.getValue()
                        .filter(operationURI, ADMS.HAS_IDENTIFIER, null)
                        .objects())
                .containsExactly(SimpleValueFactory.getInstance().createLiteral("o1500"));
    }

    @Test
    void shouldStoreYearProperty() throws RmesException {

        when(repositoryGestion.getResponseAsBoolean("unicity-labelLg1")).thenReturn(false);
        when(repositoryGestion.getResponseAsBoolean("unicity-labelLg2")).thenReturn(false);
        when(operationsObjectMapper.createId()).thenReturn("1");
        when(operationsObjectMapper.checkIfObjectExists(ObjectType.SERIES, "2")).thenReturn(true);

        when(operationsOperationQueries.checkPrefLabelUnicity("1", "prefLabelLg1", "fr"))
                .thenReturn("unicity-labelLg1");
        when(operationsOperationQueries.checkPrefLabelUnicity("1", "prefLabelLg2", "en"))
                .thenReturn("unicity-labelLg2");

        IRI operationIRI = iriFactory.operation("1");
        operationsRepository.createOperation(new OperationCommand(
                "prefLabelLg1", "prefLabelLg2", "altLabelLg1", "altLabelLg2", "2", 2024, null, null));

        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);

        verify(repositoryGestion, times(1)).loadSimpleObject(eq(operationIRI), model.capture());

        Model written = model.getValue();
        Resource graph = iriFactory.operationsGraph();
        IRI series = iriFactory.series("2");
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        assertThat(written.contexts()).containsExactly(graph);
        assertThat(written.filter(operationIRI, DCTERMS.CREATED, null).objects())
                .singleElement()
                .satisfies(
                        created -> assertThat(((Literal) created).getDatatype()).isEqualTo(XSD.DATETIME));
        assertThat(written.filter(operationIRI, DCTERMS.MODIFIED, null).objects())
                .singleElement()
                .satisfies(modified ->
                        assertThat(((Literal) modified).getDatatype()).isEqualTo(XSD.DATETIME));
        Model withoutDates = new LinkedHashModel(written);
        withoutDates.remove(operationIRI, DCTERMS.CREATED, null);
        withoutDates.remove(operationIRI, DCTERMS.MODIFIED, null);
        assertThat(withoutDates)
                .containsExactlyInAnyOrder(
                        vf.createStatement(operationIRI, RDF.TYPE, INSEE.OPERATION, graph),
                        vf.createStatement(operationIRI, ADMS.HAS_IDENTIFIER, vf.createLiteral("1"), graph),
                        vf.createStatement(
                                operationIRI, SKOS.PREF_LABEL, vf.createLiteral("prefLabelLg1", "fr"), graph),
                        vf.createStatement(
                                operationIRI, INSEE.VALIDATION_STATE, vf.createLiteral("Unpublished"), graph),
                        vf.createStatement(
                                operationIRI, SKOS.PREF_LABEL, vf.createLiteral("prefLabelLg2", "en"), graph),
                        vf.createStatement(operationIRI, SKOS.ALT_LABEL, vf.createLiteral("altLabelLg1", "fr"), graph),
                        vf.createStatement(operationIRI, SKOS.ALT_LABEL, vf.createLiteral("altLabelLg2", "en"), graph),
                        vf.createStatement(operationIRI, DCTERMS.TEMPORAL, vf.createLiteral("2024", XSD.GYEAR), graph),
                        vf.createStatement(operationIRI, DCTERMS.IS_PART_OF, series, graph),
                        vf.createStatement(series, DCTERMS.HAS_PART, operationIRI, graph));
    }

    @Test
    void setOperation_shouldNotRejectWith406_whenSeriesAlreadyHasSims() throws RmesException {
        // Une série déjà documentée par un SIMS doit pouvoir accueillir une opération (cf. ticket #1452).
        when(operationsObjectMapper.createId()).thenReturn("1");
        when(operationsObjectMapper.checkIfObjectExists(ObjectType.SERIES, "2")).thenReturn(true);

        try {
            operationsRepository.createOperation(
                    new OperationCommand("prefLabelLg1", "prefLabelLg2", null, null, "2", null, null, null));
        } catch (RmesBadRequestException e) {
            if (e.getDetails().contains("A series cannot have both a Sims and Operation(s)")) {
                fail("La création d'une opération sur une série avec SIMS ne devrait plus lever 406 : "
                        + e.getDetails());
            }
        } catch (Exception _) {
            // d'autres exceptions sont attendues car les mocks ne couvrent pas tout le flow
        }
    }

    @Test
    void setOperation_publishesOperationSavedCarryingItsSeriesIri() throws RmesException {
        when(repositoryGestion.getResponseAsBoolean(any())).thenReturn(false);
        when(operationsObjectMapper.createId()).thenReturn("o1500");
        when(operationsObjectMapper.checkIfObjectExists(ObjectType.SERIES, "s1001"))
                .thenReturn(true);
        operationsRepository.createOperation(
                new OperationCommand("Enquête emploi", "Labour survey", "EEC", "LFS", "s1001", null, null, null));

        ArgumentCaptor<OperationSaved> captor = ArgumentCaptor.forClass(OperationSaved.class);
        verify(events).publishEvent(captor.capture());
        assertThat(captor.getValue())
                .isEqualTo(new OperationSaved(
                        "http://id.insee.fr/operations/operation/o1500",
                        "o1500",
                        "http://id.insee.fr/operations/operation/s1001",
                        new BilingualLabel("Enquête emploi", "Labour survey"),
                        new BilingualLabel("EEC", "LFS")));
    }

    @Test
    void updateOperation_writesTheOperationUnderTheIdOfThePath() throws RmesException {
        when(operationsParentRepository.getValidationStatus("o1500")).thenReturn("Published");
        when(repositoryGestion.getResponseAsBoolean(any())).thenReturn(false);

        operationsRepository.setOperation(
                "o1500",
                new OperationCommand("Enquête emploi", null, null, null, null, null, null, "2026-01-01T00:00:00"));

        verify(repositoryGestion).loadSimpleObject(eq(iriFactory.objectIri(ObjectType.OPERATION, "o1500")), any());
    }

    @Test
    void setOperationValidation_announcesThePublicationWithTheOperationsGraph() throws RmesException {
        OperationsRepository repository = spy(operationsRepository);
        JSONObject operationJson = new JSONObject();
        doReturn(operationJson).when(repository).getOperationJsonById("s1500");

        repository.setOperationValidation("s1500");

        verify(operationPublication).publishOperation("s1500", operationJson, iriFactory.operationsGraph());
        ArgumentCaptor<ObjectPublished> event = ArgumentCaptor.forClass(ObjectPublished.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue())
                .isEqualTo(new ObjectPublished(
                        iriFactory.objectIri(ObjectType.OPERATION, "s1500"),
                        SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/graphes/operations")));
    }
}
