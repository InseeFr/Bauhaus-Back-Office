package fr.insee.rmes.bauhaus_services.operations.series;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/bauhaus_services/operations/series/SeriesUtilsTest.java
import fr.insee.rmes.onion.domain.exceptions.RmesException;
=======
>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/test/java/fr/insee/rmes/bauhaus_services/operations/series/SeriesUtilsTest.java
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
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
import org.springframework.beans.factory.annotation.Autowired;

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
        SeriesUtils indicatorsUtils = new SeriesUtils(true, "fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null);

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
        SeriesUtils indicatorsUtils = new SeriesUtils(true, "fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null, null, null, null, null);

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

}