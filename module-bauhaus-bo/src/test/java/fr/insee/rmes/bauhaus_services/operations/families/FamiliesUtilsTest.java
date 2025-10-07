package fr.insee.rmes.bauhaus_services.operations.families;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamiliesUtilsTest {

    @Mock
    private RepositoryGestion repositoryGestion;


    @Test
    void shouldAddAbstractPropertyWithNewSyntaxIfFeatureFlagTrue() throws RmesException {
        doNothing().when(repositoryGestion).deleteObject(any(), any());
        FamiliesUtils familiesUtils = new FamiliesUtils(true, null, null, null, repositoryGestion, "fr", "en");

        var family = new Family();
        family.setId("1");
        family.setAbstractLg1("AbstractLg1");
        family.setAbstractLg2("setAbstractLg2");
        IRI familyIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + family.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        familiesUtils.addAbstractToFamily(family, model, familyIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"));
        verify(repositoryGestion, times(2)).deleteObject(any(), any());

        Assertions.assertEquals(model.subjects().toArray()[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));
        Assertions.assertEquals(model.subjects().toArray()[1], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/fr"));
        Assertions.assertEquals(model.subjects().toArray()[2], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1/resume/en"));
    }

    @Test
    void shouldAddAbstractPropertyWithOldSyntaxIfFeatureFlagFalse() throws RmesException {
        FamiliesUtils familiesUtils = new FamiliesUtils(true, null, null, null, repositoryGestion, "fr", "en");

        var family = new Family();
        family.setId("1");
        family.setAbstractLg1("AbstractLg1");
        family.setAbstractLg2("AbstractLg1");
        IRI familyIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + family.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        familiesUtils.addAbstractToFamily(family, model, familyIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"));


        Assertions.assertEquals(model.subjects().toArray()[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));

        Assertions.assertEquals(model.predicates().toArray()[0], simpleValueFactory.createIRI(DCTERMS.ABSTRACT.toString()));

        Assertions.assertEquals("\"<p>AbstractLg1</p>\"@fr", model.objects().toArray()[0].toString());
        Assertions.assertEquals("\"<p>AbstractLg1</p>\"@en", model.objects().toArray()[1].toString());
    }

    @Test
    void shouldThrowRmesNotFoundExceptionWhenFamilyIsNull()  {
        FamiliesUtils familiesUtils = new FamiliesUtils(true, null, null, null, null, "fr", "en");
        RmesException exception = assertThrows(RmesNotFoundException.class, () ->  familiesUtils.createRdfFamily(null,null));
        org.assertj.core.api.Assertions.assertThat(exception.getDetails()).contains("{\"details\":\"Can't read request body\",\"message\":\"541 : No id found\"}");
    }

    @Test
    void shouldThrowRmesNotFoundExceptionWhenIdIsAbsent(){
        FamiliesUtils familiesUtils = new FamiliesUtils(true, null, null, null,null, "fr", "en");
        Family familyCreate = new Family();
        familyCreate.setCreated("today");
        RmesException exception = assertThrows(RmesNotFoundException.class, () ->  familiesUtils.createRdfFamily(familyCreate,null));
        org.assertj.core.api.Assertions.assertThat(exception.getDetails()).contains("{\"details\":\"Can't read request body\",\"message\":\"541 : No id found\"}");
    }

    @Test
    void shouldThrowRmesNotFoundExceptionWhenPrefLabelLg1IsAbsent() {
        FamiliesUtils familiesUtils = new FamiliesUtils(true, null, null, null,null, "fr", "en");
        Family familyCreate = new Family();
        familyCreate.setId("idExample");
        familyCreate.setAbstractLg1("");
        RmesException exception = assertThrows(RmesNotFoundException.class, () ->  familiesUtils.createRdfFamily(familyCreate,null));
        org.assertj.core.api.Assertions.assertThat(exception.getDetails()).contains("{\"details\":\"Can't read request body\",\"message\":\"542 : prefLabelLg1 not found\"}");
    }

}