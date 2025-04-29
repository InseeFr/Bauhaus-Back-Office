package fr.insee.rmes.bauhaus_services.operations.families;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesBadRequestException;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FamiliesUtilsTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    @Test
    void shouldReturnAnExceptionWhenTitleIsNotPresentAtLeast() throws RmesException, JsonProcessingException {

        String body= "{\n" +
                "  \"prefLabelLg3\": \"Activité, production et chiffre d'affaires\",\n" +
                "  \"prefLabelLg2\": \"Activity, production and turnover\",\n" +
                "  \"series\": [\n" +
                "    {\n" +
                "      \"labelLg2\": \"Monthly branch surveys\",\n" +
                "      \"labelLg1\": \"Enquêtes mensuelles de branche\",\n" +
                "      \"id\": \"s1006\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"labelLg2\": \"Survey on the impact of the health crisis on the businesses organisation and activity\",\n" +
                "      \"labelLg1\": \"Enquête sur l'impact de la crise sanitaire sur l'organisation et l'activité des entreprises\",\n" +
                "      \"id\": \"s1044\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"labelLg2\": \"Monthly survey of large-scale food retail activities\",\n" +
                "      \"labelLg1\": \"Enquête mensuelle sur l'activité des grandes surfaces alimentaires\",\n" +
                "      \"id\": \"s1222\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"labelLg2\": \"Computation of activity and turnover indices\",\n" +
                "      \"labelLg1\": \"Élaboration des indicateurs d'activité et de chiffre d'affaires\",\n" +
                "      \"id\": \"s1284\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"labelLg2\": \"Test 4\",\n" +
                "      \"labelLg1\": \"Test 4\",\n" +
                "      \"id\": \"s2284\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"modified\": \"2024-07-17T11:16:55.419150048\",\n" +
                "  \"id\": \"s82\",\n" +
                "  \"validationState\": \"Modified\"\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Family family = mapper.readValue(body,Family.class);

        RmesException exception = assertThrows(RmesBadRequestException.class, () -> FamiliesUtils.verifyBodyToCreateFamily(family));
        assertThat(exception.getDetails()).contains("Required title not entered by user.");
    }


    @Test
    void shouldAddAbstractPropertyWithNewSyntaxIfFeatureFlagTrue() throws RmesException {
        doNothing().when(repositoryGestion).deleteObject(any(), any());
        FamiliesUtils familiesUtils = new FamiliesUtils(true, null, null, null, repositoryGestion, null, "fr", "en");

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
        FamiliesUtils familiesUtils = new FamiliesUtils(true, null, null, null, repositoryGestion, null, "fr", "en");

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

}