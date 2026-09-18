package fr.insee.rmes.bauhaus_services.operations.series;

import static fr.insee.rmes.bauhaus_services.operations.OperationsRdfModelAssertions.assertAbstractsWrittenAsPlainMarkdownLiterals;
import static fr.insee.rmes.bauhaus_services.operations.OperationsRdfModelAssertions.assertSingleIriObject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.OperationsObjectMapper;
import fr.insee.rmes.bauhaus_services.operations.series.validation.SeriesValidator;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.modules.operation.domain.event.BilingualLabel;
import fr.insee.rmes.modules.operation.domain.event.SeriesSaved;
import fr.insee.rmes.modules.operations.series.domain.model.Series;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

@AppSpringBootTest
@ExtendWith(MockitoExtension.class)
class SeriesRepositoryTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Autowired
    private OperationsObjectMapper operationsObjectMapper;

    @Test
    void shouldAddAbstractPropertyAsPlainMarkdownLiterals() {
        SeriesRepository seriesRepository = seriesRepository(null, null);

        var series = new Series();
        series.setId("1");
        series.setAbstractLg1("AbstractLg1");
        series.setAbstractLg2("setAbstractLg2");
        IRI seriesIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + series.getId());

        assertAbstractsWrittenAsPlainMarkdownLiterals(seriesIri, seriesRepository::addMulltiLangValues);
    }

    private static final IRI TEST_GRAPH = SimpleValueFactory.getInstance().createIRI("http://test/operations");

    @Test
    void createRdfSeries_addsAdmsIdentifierTriple() throws RmesException {
        SeriesValidator validator = mock(SeriesValidator.class);
        SeriesRepository seriesRepository = seriesRepository(validator, null);
        Series series = new Series();
        series.setId("s2000");
        series.setPrefLabelLg1("Série de test");

        seriesRepository.createRdfSeries(series, null, ValidationStatus.UNPUBLISHED);

        ArgumentCaptor<Model> captor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryGestion).loadObjectWithReplaceLinks(any(), captor.capture());
        IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES, "s2000");
        assertThat(captor.getValue()
                        .filter(seriesURI, ADMS.HAS_IDENTIFIER, null)
                        .objects())
                .containsExactly(SimpleValueFactory.getInstance().createLiteral("s2000"));
    }

    @Test
    void addOperationLinksOrganization_writesIriPassthrough_whenLinkIdIsAlreadyAnIri() throws RmesException {
        assertOrganisationLinkWrittenAsIri("http://bauhaus/organisations/DG75-A001", DCTERMS.PUBLISHER);
    }

    @Test
    void addOperationLinksOrganization_resolvesLegacyIdViaLookup() throws RmesException {
        assertOrganisationLinkWrittenAsIri("DG75-A001", DCTERMS.CONTRIBUTOR);
    }

    /** Le lien d'organisation {@code linkId} est résolu par le lookup et écrit comme une IRI. */
    private void assertOrganisationLinkWrittenAsIri(String linkId, IRI predicate) throws RmesException {
        OrganisationLookup lookup = mock(OrganisationLookup.class);
        when(lookup.resolve(linkId)).thenReturn(Optional.of("http://bauhaus/organisations/DG75-A001"));
        SeriesRepository seriesRepository = seriesRepository(null, lookup);
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI seriesURI = vf.createIRI("http://bauhaus/series/s1");
        Model model = new LinkedHashModel();
        OperationsLink link = new OperationsLink();
        link.id = linkId;

        seriesRepository.addOperationLinksOrganization(List.of(link), predicate, model, seriesURI, TEST_GRAPH);

        assertSingleIriObject(model, seriesURI, predicate, "http://bauhaus/organisations/DG75-A001");
    }

    @Test
    void setSeries_shouldNotRejectWith406_whenBodyContainsBothIdSimsAndOperations() {
        SeriesRepository seriesRepository = seriesRepository(null, null);
        String body =
                "{\"idSims\":\"sims-1\",\"operations\":[{\"id\":\"op1\",\"labelLg1\":\"L1\",\"labelLg2\":\"L2\"}]}";

        try {
            seriesRepository.setSeries("1", body);
        } catch (RmesNotAcceptableException e) {
            if (e.getDetails().contains("A series cannot have both a Sims and Operation(s)")) {
                fail("La mise à jour d'une série combinant idSims et operations ne devrait plus lever 406 : "
                        + e.getDetails());
            }
        } catch (Exception _) {
            // d'autres exceptions sont attendues car les mocks ne couvrent pas tout le flow
        }
    }

    @Test
    void addCreators_writesEachCreatorAsAnIriTriple() {
        SeriesRepository seriesRepository = seriesRepository(null, null);
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI seriesURI = vf.createIRI("http://bauhaus/series/s1");
        Model model = new LinkedHashModel();

        seriesRepository.addCreators(
                model,
                seriesURI,
                List.of("http://bauhaus/organisations/DG75-A001", "http://bauhaus/organisations/DG75-B002"),
                TEST_GRAPH);

        IRI dcCreator = vf.createIRI(DC.CREATOR.toString());
        List<Value> creators = model.filter(seriesURI, dcCreator, null).stream()
                .map(Statement::getObject)
                .toList();
        assertThat(creators).hasSize(2);
        assertThat(creators).allMatch(IRI.class::isInstance, "every dc:creator object must be an IRI, not a literal");
        assertThat(creators)
                .extracting(Value::stringValue)
                .containsExactlyInAnyOrder(
                        "http://bauhaus/organisations/DG75-A001", "http://bauhaus/organisations/DG75-B002");
    }

    @Test
    void setSeries_publishesSeriesSavedWithThePublicationIriAndBothLabels() throws RmesException {
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        BauhausUriBuilder uriBuilder = mock(BauhausUriBuilder.class);
        when(uriBuilder.getCompleteUriPublication("series", "s1001"))
                .thenReturn("http://id.insee.fr/operations/serie/s1001");
        OperationsParentRepository parents = mock(OperationsParentRepository.class);
        when(parents.getFamOpSerValidationStatus("s1001")).thenReturn(ValidationStatus.UNPUBLISHED.getValue());
        SeriesRepository seriesRepository = new SeriesRepository(
                new BauhausLanguagesProperties("fr", "en"),
                repositoryGestion,
                null,
                null,
                operationsObjectMapper,
                parents,
                null,
                mock(DocumentationsUtils.class),
                uriBuilder,
                mock(SeriesValidator.class),
                null,
                null,
                events);

        seriesRepository.setSeries("s1001", """
                {"id":"s1001","prefLabelLg1":"Recensement","prefLabelLg2":"Census",                "altLabelLg1":"RP","altLabelLg2":"CENS"}""");

        ArgumentCaptor<SeriesSaved> captor = ArgumentCaptor.forClass(SeriesSaved.class);
        verify(events).publishEvent(captor.capture());
        assertThat(captor.getValue())
                .isEqualTo(new SeriesSaved(
                        "http://id.insee.fr/operations/serie/s1001",
                        "s1001",
                        new BilingualLabel("Recensement", "Census"),
                        new BilingualLabel("RP", "CENS")));
    }

    @Test
    void createSeries_publishesSeriesSaved() throws RmesException {
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        BauhausUriBuilder uriBuilder = mock(BauhausUriBuilder.class);
        when(uriBuilder.getCompleteUriPublication("series", "s1001"))
                .thenReturn("http://id.insee.fr/operations/serie/s1001");
        OperationsObjectMapper objectMapper = mock(OperationsObjectMapper.class);
        when(objectMapper.checkIfObjectExists(ObjectType.FAMILY, "f1")).thenReturn(true);
        SeriesRepository seriesRepository = new SeriesRepository(
                new BauhausLanguagesProperties("fr", "en"),
                repositoryGestion,
                null,
                null,
                objectMapper,
                null,
                null,
                null,
                uriBuilder,
                mock(SeriesValidator.class),
                null,
                null,
                events);

        seriesRepository.createSeries("""
                {"id":"s1001","prefLabelLg1":"Recensement","family":{"id":"f1"}}""");

        ArgumentCaptor<SeriesSaved> captor = ArgumentCaptor.forClass(SeriesSaved.class);
        verify(events).publishEvent(captor.capture());
        assertThat(captor.getValue().iri()).isEqualTo("http://id.insee.fr/operations/serie/s1001");
        assertThat(captor.getValue().prefLabel()).isEqualTo(new BilingualLabel("Recensement", null));
    }

    private SeriesRepository seriesRepository(SeriesValidator validator, OrganisationLookup organisationLookup) {
        return new SeriesRepository(
                new BauhausLanguagesProperties("fr", "en"),
                repositoryGestion,
                null,
                null,
                operationsObjectMapper,
                null,
                null,
                null,
                null,
                validator,
                null,
                organisationLookup,
                null);
    }
}
