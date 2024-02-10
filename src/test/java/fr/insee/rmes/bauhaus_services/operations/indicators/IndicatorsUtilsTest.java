package fr.insee.rmes.bauhaus_services.operations.indicators;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Family;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndicatorsUtilsTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Test
    void shouldAddAbstractPropertyWithNewSyntaxIfFeatureFlagTrue() throws RmesException {
        doNothing().when(repositoryGestion).deleteObject(any(), any());
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, null, null, null, null, null, "fr", "en");

        var family = new Family();
        family.setId("1");
        family.setAbstractLg1("AbstractLg1");
        family.setAbstractLg2("setAbstractLg2");
        IRI familyIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + family.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        indicatorsUtils.addMulltiLangValues(model, familyIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"), "fr", "en", DCTERMS.ABSTRACT);
        verify(repositoryGestion, times(2)).deleteObject(any(), any());

        Assertions.assertEquals(model.subjects().toArray()[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));
        Assertions.assertEquals(model.subjects().toArray()[1], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/fr"));
        Assertions.assertEquals(model.subjects().toArray()[2], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/en"));
    }

    @Test
    void shouldAddAbstractPropertyWithOldSyntaxIfFeatureFlagFalse() throws RmesException {
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(false, repositoryGestion, null, null, null, null, null, null, null, null, "fr", "en");

        var family = new Family();
        family.setId("1");
        family.setAbstractLg1("AbstractLg1");
        family.setAbstractLg2("AbstractLg1");
        IRI familyIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + family.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        indicatorsUtils.addMulltiLangValues(model, familyIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"), "fr", "en", DCTERMS.ABSTRACT);


        Assertions.assertEquals(model.subjects().toArray()[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));

        Assertions.assertEquals(model.predicates().toArray()[0], simpleValueFactory.createIRI(DCTERMS.ABSTRACT.toString()));

        Assertions.assertEquals(model.objects().toArray()[0].toString(), "\"<p>fr</p>\"@fr");
        Assertions.assertEquals(model.objects().toArray()[1].toString(), "\"<p>en</p>\"@en");
    }

}