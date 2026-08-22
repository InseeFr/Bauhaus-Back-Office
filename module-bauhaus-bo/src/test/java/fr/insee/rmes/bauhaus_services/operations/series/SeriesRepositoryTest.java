package fr.insee.rmes.bauhaus_services.operations.series;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.operations.series.validation.SeriesValidator;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.modules.operations.series.domain.model.Series;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@AppSpringBootTest
@ExtendWith(MockitoExtension.class)
class SeriesRepositoryTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Autowired
    private FamOpeSerIndUtils famOpeSerIndUtils;

    @Test
    void shouldAddAbstractPropertyAsPlainMarkdownLiterals() {
        SeriesRepository seriesRepository = new SeriesRepository("fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null, null, null);

        var series = new Series();
        series.setId("1");
        series.setAbstractLg1("AbstractLg1");
        series.setAbstractLg2("setAbstractLg2");
        IRI seriesIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + series.getId());

        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        seriesRepository.addMulltiLangValues(model, seriesIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"), "fr", "en", DCTERMS.ABSTRACT);


        Assertions.assertEquals(model.subjects().toArray()[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));

        Assertions.assertEquals(model.predicates().toArray()[0], simpleValueFactory.createIRI(DCTERMS.ABSTRACT.toString()));

        Assertions.assertEquals("\"<p>fr</p>\"@fr", model.objects().toArray()[0].toString());
        Assertions.assertEquals("\"<p>en</p>\"@en", model.objects().toArray()[1].toString());
    }

    private static final IRI TEST_GRAPH = SimpleValueFactory.getInstance().createIRI("http://test/operations");

    @Test
    void createRdfSeries_addsAdmsIdentifierTriple() throws RmesException {
        SeriesValidator validator = mock(SeriesValidator.class);
        SeriesRepository seriesRepository = new SeriesRepository("fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, validator, null, null);
        Series series = new Series();
        series.setId("s2000");
        series.setPrefLabelLg1("Série de test");

        seriesRepository.createRdfSeries(series, null, ValidationStatus.UNPUBLISHED);

        ArgumentCaptor<Model> captor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryGestion).loadObjectWithReplaceLinks(any(), captor.capture());
        IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES, "s2000");
        assertThat(captor.getValue().filter(seriesURI, ADMS.HAS_IDENTIFIER, null).objects())
                .containsExactly(SimpleValueFactory.getInstance().createLiteral("s2000"));
    }

    @Test
    void addOperationLinksOrganization_writesIriPassthrough_whenLinkIdIsAlreadyAnIri() throws RmesException {
        OrganisationLookup lookup = mock(OrganisationLookup.class);
        when(lookup.resolve("http://bauhaus/organisations/DG75-A001"))
                .thenReturn(Optional.of("http://bauhaus/organisations/DG75-A001"));
        SeriesRepository seriesRepository = new SeriesRepository("fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null, null, lookup);
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI seriesURI = vf.createIRI("http://bauhaus/series/s1");
        Model model = new LinkedHashModel();
        OperationsLink link = new OperationsLink();
        link.id = "http://bauhaus/organisations/DG75-A001";

        seriesRepository.addOperationLinksOrganization(List.of(link), DCTERMS.PUBLISHER, model, seriesURI, TEST_GRAPH);

        IRI publisher = vf.createIRI(DCTERMS.PUBLISHER.toString());
        List<Value> publishers = model.filter(seriesURI, publisher, null).stream()
                .map(Statement::getObject)
                .toList();
        assertThat(publishers).hasSize(1);
        assertThat(publishers.get(0)).isInstanceOf(IRI.class);
        assertThat(publishers.get(0).stringValue()).isEqualTo("http://bauhaus/organisations/DG75-A001");
    }

    @Test
    void addOperationLinksOrganization_resolvesLegacyIdViaLookup() throws RmesException {
        OrganisationLookup lookup = mock(OrganisationLookup.class);
        when(lookup.resolve("DG75-A001"))
                .thenReturn(Optional.of("http://bauhaus/organisations/DG75-A001"));
        SeriesRepository seriesRepository = new SeriesRepository("fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null, null, lookup);
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI seriesURI = vf.createIRI("http://bauhaus/series/s1");
        Model model = new LinkedHashModel();
        OperationsLink link = new OperationsLink();
        link.id = "DG75-A001";

        seriesRepository.addOperationLinksOrganization(List.of(link), DCTERMS.CONTRIBUTOR, model, seriesURI, TEST_GRAPH);

        IRI contributor = vf.createIRI(DCTERMS.CONTRIBUTOR.toString());
        List<Value> contributors = model.filter(seriesURI, contributor, null).stream()
                .map(Statement::getObject)
                .toList();
        assertThat(contributors).hasSize(1);
        assertThat(contributors.get(0)).isInstanceOf(IRI.class);
        assertThat(contributors.get(0).stringValue()).isEqualTo("http://bauhaus/organisations/DG75-A001");
    }

    @Test
    void setSeries_shouldNotRejectWith406_whenBodyContainsBothIdSimsAndOperations() {
        SeriesRepository seriesRepository = new SeriesRepository("fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null, null, null);
        String body = "{\"idSims\":\"sims-1\",\"operations\":[{\"id\":\"op1\",\"labelLg1\":\"L1\",\"labelLg2\":\"L2\"}]}";

        try {
            seriesRepository.setSeries("1", body);
        } catch (RmesNotAcceptableException e) {
            if (e.getDetails().contains("A series cannot have both a Sims and Operation(s)")) {
                fail("La mise à jour d'une série combinant idSims et operations ne devrait plus lever 406 : " + e.getDetails());
            }
        } catch (Exception ignored) {
            // d'autres exceptions sont attendues car les mocks ne couvrent pas tout le flow
        }
    }

    @Test
    void addCreators_writesEachCreatorAsAnIriTriple() {
        SeriesRepository seriesRepository = new SeriesRepository("fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null, null, null);
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI seriesURI = vf.createIRI("http://bauhaus/series/s1");
        Model model = new LinkedHashModel();

        seriesRepository.addCreators(model, seriesURI, List.of(
                "http://bauhaus/organisations/DG75-A001",
                "http://bauhaus/organisations/DG75-B002"), TEST_GRAPH);

        IRI dcCreator = vf.createIRI(DC.CREATOR.toString());
        List<Value> creators = model.filter(seriesURI, dcCreator, null).stream()
                .map(Statement::getObject)
                .toList();
        assertThat(creators).hasSize(2);
        assertThat(creators).allMatch(value -> value instanceof IRI,
                "every dc:creator object must be an IRI, not a literal");
        assertThat(creators).extracting(Value::stringValue)
                .containsExactlyInAnyOrder(
                        "http://bauhaus/organisations/DG75-A001",
                        "http://bauhaus/organisations/DG75-B002");
    }

}