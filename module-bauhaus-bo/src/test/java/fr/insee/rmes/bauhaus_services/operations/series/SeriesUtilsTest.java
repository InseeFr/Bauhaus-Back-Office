package fr.insee.rmes.bauhaus_services.operations.series;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.model.links.OperationsLink;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@AppSpringBootTest
@ExtendWith(MockitoExtension.class)
class SeriesUtilsTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Autowired
    private FamOpeSerIndUtils famOpeSerIndUtils;

    @Test
    void shouldAddAbstractPropertyWithNewSyntaxIfFeatureFlagTrue() throws RmesException {
        doNothing().when(repositoryGestion).deleteObject(any(), any());
        SeriesUtils indicatorsUtils = new SeriesUtils(true, "fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null, null, null);

        var series = new Series();
        series.setId("1");
        series.setAbstractLg1("AbstractLg1");
        series.setAbstractLg2("setAbstractLg2");
        IRI seriesIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + series.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        indicatorsUtils.addMulltiLangValues(model, seriesIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"), "fr", "en", DCTERMS.ABSTRACT);
        verify(repositoryGestion, times(2)).deleteObject(any(), any());

        Assertions.assertEquals(model.subjects().toArray()[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));
        Assertions.assertEquals(model.subjects().toArray()[1], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/fr"));
        Assertions.assertEquals(model.subjects().toArray()[2], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/en"));
    }

    @Test
    void shouldAddAbstractPropertyWithOldSyntaxIfFeatureFlagFalse() throws RmesException {
        SeriesUtils indicatorsUtils = new SeriesUtils(true, "fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null, null, null);

        var series = new Series();
        series.setId("1");
        series.setAbstractLg1("AbstractLg1");
        series.setAbstractLg2("setAbstractLg2");
        IRI seriesIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + series.getId());

        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        indicatorsUtils.addMulltiLangValues(model, seriesIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"), "fr", "en", DCTERMS.ABSTRACT);


        Assertions.assertEquals(model.subjects().toArray()[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));

        Assertions.assertEquals(model.predicates().toArray()[0], simpleValueFactory.createIRI(DCTERMS.ABSTRACT.toString()));

        Assertions.assertEquals("\"<p>fr</p>\"@fr", model.objects().toArray()[0].toString());
        Assertions.assertEquals("\"<p>en</p>\"@en", model.objects().toArray()[1].toString());
    }

    private static final IRI TEST_GRAPH = SimpleValueFactory.getInstance().createIRI("http://test/operations");

    @Test
    void addOperationLinksOrganization_writesIriPassthrough_whenLinkIdIsAlreadyAnIri() throws RmesException {
        OrganisationLookup lookup = mock(OrganisationLookup.class);
        when(lookup.resolve("http://bauhaus/organisations/DG75-A001"))
                .thenReturn(java.util.Optional.of("http://bauhaus/organisations/DG75-A001"));
        SeriesUtils seriesUtils = new SeriesUtils(false, "fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null, null, lookup);
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI seriesURI = vf.createIRI("http://bauhaus/series/s1");
        Model model = new LinkedHashModel();
        OperationsLink link = new OperationsLink();
        link.id = "http://bauhaus/organisations/DG75-A001";

        seriesUtils.addOperationLinksOrganization(List.of(link), DCTERMS.PUBLISHER, model, seriesURI, TEST_GRAPH);

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
                .thenReturn(java.util.Optional.of("http://bauhaus/organisations/DG75-A001"));
        SeriesUtils seriesUtils = new SeriesUtils(false, "fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null, null, lookup);
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI seriesURI = vf.createIRI("http://bauhaus/series/s1");
        Model model = new LinkedHashModel();
        OperationsLink link = new OperationsLink();
        link.id = "DG75-A001";

        seriesUtils.addOperationLinksOrganization(List.of(link), DCTERMS.CONTRIBUTOR, model, seriesURI, TEST_GRAPH);

        IRI contributor = vf.createIRI(DCTERMS.CONTRIBUTOR.toString());
        List<Value> contributors = model.filter(seriesURI, contributor, null).stream()
                .map(Statement::getObject)
                .toList();
        assertThat(contributors).hasSize(1);
        assertThat(contributors.get(0)).isInstanceOf(IRI.class);
        assertThat(contributors.get(0).stringValue()).isEqualTo("http://bauhaus/organisations/DG75-A001");
    }

    @Test
    void addCreators_writesEachCreatorAsAnIriTriple() {
        SeriesUtils seriesUtils = new SeriesUtils(false, "fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null, null, null);
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI seriesURI = vf.createIRI("http://bauhaus/series/s1");
        Model model = new LinkedHashModel();

        seriesUtils.addCreators(model, seriesURI, List.of(
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