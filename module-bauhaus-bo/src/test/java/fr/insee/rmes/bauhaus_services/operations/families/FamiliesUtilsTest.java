package fr.insee.rmes.bauhaus_services.operations.families;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.operations.Family;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamiliesUtilsTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    @BeforeAll
    static void initRdfUtils() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(new BauhausUriBuilder("http://bauhaus/publication/", "http://bauhaus/", p -> Optional.of("/operations")));
    }

    @Test
    void createRdfFamily_addsAdmsIdentifierTriple() throws RmesException {
        FamiliesUtils familiesUtils = new FamiliesUtils(null, null, null, repositoryGestion, "fr", "en", null);
        Family family = new Family();
        family.setId("s1");
        family.prefLabelLg1 = "Famille de test";

        familiesUtils.createRdfFamily(family, ValidationStatus.UNPUBLISHED);

        ArgumentCaptor<Model> captor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryGestion).loadSimpleObject(any(), captor.capture());
        IRI familyURI = RdfUtils.objectIRI(ObjectType.FAMILY, "s1");
        assertThat(captor.getValue().filter(familyURI, ADMS.HAS_IDENTIFIER, null).objects())
                .containsExactly(SimpleValueFactory.getInstance().createLiteral("s1"));
    }


    @Test
    void shouldAddAbstractPropertyAsPlainMarkdownLiterals() {
        FamiliesUtils familiesUtils = new FamiliesUtils(null, null, null, repositoryGestion, "fr", "en", null);

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
        FamiliesUtils familiesUtils = new FamiliesUtils(null, null, null, null, "fr", "en", null);
        RmesException exception = assertThrows(RmesNotFoundException.class, () ->  familiesUtils.createRdfFamily(null,null));
        assertThat(exception.getDetails()).contains("{\"details\":\"Can't read request body\",\"message\":\"541 : No id found\"}");
    }

    @Test
    void shouldThrowRmesNotFoundExceptionWhenIdIsAbsent(){
        FamiliesUtils familiesUtils = new FamiliesUtils(null, null, null, null, "fr", "en", null);
        Family familyCreate = new Family();
        familyCreate.setCreated("today");
        RmesException exception = assertThrows(RmesNotFoundException.class, () ->  familiesUtils.createRdfFamily(familyCreate,null));
        assertThat(exception.getDetails()).contains("{\"details\":\"Can't read request body\",\"message\":\"541 : No id found\"}");
    }

    @Test
    void shouldThrowRmesNotFoundExceptionWhenPrefLabelLg1IsAbsent() {
        FamiliesUtils familiesUtils = new FamiliesUtils(null, null, null, null, "fr", "en", null);
        Family familyCreate = new Family();
        familyCreate.setId("idExample");
        familyCreate.setAbstractLg1("");
        RmesException exception = assertThrows(RmesNotFoundException.class, () ->  familiesUtils.createRdfFamily(familyCreate,null));
        assertThat(exception.getDetails()).contains("{\"details\":\"Can't read request body\",\"message\":\"542 : prefLabelLg1 not found\"}");
    }

}