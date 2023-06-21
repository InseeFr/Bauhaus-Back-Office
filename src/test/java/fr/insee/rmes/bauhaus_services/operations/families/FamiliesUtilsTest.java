package fr.insee.rmes.bauhaus_services.operations.families;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Family;
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


public class FamiliesUtilsTest {

    @InjectMocks
    FamiliesUtils familiesUtils;

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
        Family family = new Family();
        family.setId("1");
        family.setAbstractLg1("AbstractLg1");
        family.setAbstractLg2("setAbstractLg2");
        family.setPrefLabelLg1("prefLabelLg1");
        IRI familyIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + family.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        familiesUtils.addAbstractToFamily(family, model, familyIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"));
        verify(repoGestion, times(2)).deleteObject(any(), any());

        Assertions.assertEquals(model.subjects().toArray()[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));
        Assertions.assertEquals(model.subjects().toArray()[1], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/FR"));
        Assertions.assertEquals(model.subjects().toArray()[2], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/EN"));
    }

}
