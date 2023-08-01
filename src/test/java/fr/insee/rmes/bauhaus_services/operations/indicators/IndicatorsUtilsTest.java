package fr.insee.rmes.bauhaus_services.operations.indicators;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Indicator;
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


public class IndicatorsUtilsTest {

    @InjectMocks
    IndicatorsUtils indicatorsUtils;

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
        Indicator indicator = new Indicator();
        indicator.setId("1");
        indicator.setAbstractLg1("setAbstractLg1");
        indicator.setAbstractLg2("setAbstractLg2");
        IRI indicatorIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + indicator.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        indicatorsUtils.addAbstractToIndicator(indicator, model, indicatorIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"));
        verify(repoGestion, times(2)).deleteObject(any(), any());


        Assertions.assertEquals(model.size(),10);
        Object[] subjects = model.subjects().toArray();
        Assertions.assertEquals(subjects[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));
        Assertions.assertEquals(subjects[1], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/FR"));
        Assertions.assertEquals(subjects[2], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/EN"));
    }

    @Test
    void shouldAddHistoryProperty() throws RmesException {
        when(config.getLg1()).thenReturn("FR");
        when(config.getLg2()).thenReturn("EN");
        Indicator indicator = new Indicator();
        indicator.setId("1");
        indicator.setHistoryNoteLg1("setHistoryNoteLg1");
        indicator.setHistoryNoteLg2("setHistoryNoteLg2");
        IRI indicatorIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + indicator.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        indicatorsUtils.addHistoryToIndicator(indicator, model, indicatorIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"));
        verify(repoGestion, times(2)).deleteObject(any(), any());


        Assertions.assertEquals(model.size(), 10);
        Object[] subjects = model.subjects().toArray();
        Assertions.assertEquals(subjects[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));
        Assertions.assertEquals(subjects[1], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/history/FR"));
        Assertions.assertEquals(subjects[2], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/history/EN"));
    }

}