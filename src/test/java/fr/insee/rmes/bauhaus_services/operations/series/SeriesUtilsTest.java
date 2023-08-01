package fr.insee.rmes.bauhaus_services.operations.series;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Series;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;


public class SeriesUtilsTest {

    @InjectMocks
    SeriesUtils seriesUtils;

    @Mock
    private RepositoryGestion repoGestion;

    @Mock
    Config config;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldAddAbstractProperty() throws RmesException {
        when(config.getLg1()).thenReturn("FR");
        when(config.getLg2()).thenReturn("EN");
        Series series = new Series();
        series.setId("1");
        series.setAbstractLg1("setAbstractLg1");
        series.setAbstractLg2("setAbstractLg2");
        IRI seriesIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + series.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        seriesUtils.addAbstractToSeries(series, model, seriesIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"));
        verify(repoGestion, times(2)).deleteObject(any(), any());


        Assertions.assertEquals(model.size(), 10);
        Object[] subjects = model.subjects().toArray();
        Assertions.assertEquals(subjects[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));
        Assertions.assertEquals(subjects[1], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/FR"));
        Assertions.assertEquals(subjects[2], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/EN"));
    }

    @Test
    void shouldAddHistoryProperty() throws RmesException {
        when(config.getLg1()).thenReturn("FR");
        when(config.getLg2()).thenReturn("EN");
        Series series = new Series();
        series.setId("1");
        series.setHistoryNoteLg1("setHistoryNoteLg1");
        series.setHistoryNoteLg2("setHistoryNoteLg2");
        IRI seriesIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + series.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        seriesUtils.addHistoryToSeries(series, model, seriesIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"));
        verify(repoGestion, times(2)).deleteObject(any(), any());


        Assertions.assertEquals(model.size(), 10);
        Object[] subjects = model.subjects().toArray();
        Assertions.assertEquals(subjects[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));
        Assertions.assertEquals(subjects[1], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/history/FR"));
        Assertions.assertEquals(subjects[2], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/history/EN"));
    }

}