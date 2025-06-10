package fr.insee.rmes.bauhaus_services.operations.series;

import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Series;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.DeserializationFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class SeriesUtilsTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Autowired
    private FamOpeSerIndUtils famOpeSerIndUtils;

    @Test
    void shouldBodyIncludesTheRequiredArgumentsToCreateSeries() throws IOException {

        String body = "{\n" +
                "  \"idSims\": \"2205\",\n" +
                "  \"prefLabelLg1\": \"À une n’ouvelle série\",\n" +
                "  \"prefLabelLg2\": \"Yes a n’ew serie\",\n" +
                "  \"created\": \"2025-02-11T13:40:36.678942797\",\n" +
                "  \"replaces\": [],\n" +
                "  \"dataCollectors\": [],\n" +
                "  \"creators\": [\n" +
                "    \"DG75-L201\"\n" +
                "  ],\n" +
                "  \"accrualPeriodicityList\": \"CL_FREQ\",\n" +
                "  \"seeAlso\": [],\n" +
                "  \"typeCode\": \"S\",\n" +
                "  \"typeList\": \"CL_SOURCE_CATEGORY\",\n" +
                "  \"modified\": \"2025-03-06T10:49:19.97723051\",\n" +
                "  \"publishers\": [],\n" +
                "  \"accrualPeriodicityCode\": \"A\",\n" +
                "  \"id\": \"s2288\",\n" +
                "  \"altLabelLg2\": \"YNS\",\n" +
                "  \"contributors\": [],\n" +
                "  \"altLabelLg1\": \"ANS\",\n" +
                "  \"family\": {\n" +
                "    \"labelLg2\": \"test CG\",\n" +
                "    \"labelLg1\": \"test CG\",\n" +
                "    \"id\": \"s2245\"\n" +
                "  },\n" +
                "  \"isReplacedBy\": [],\n" +
                "  \"validationState\": \"Modified\"\n" +
                "} ";

        JSONObject seriesJson = new JSONObject(body);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Series series = mapper.readValue(seriesJson.toString(), Series.class);

        List<Boolean> conditions = new ArrayList<>();
        conditions.add(series.getPrefLabelLg1()==null);
        conditions.add(series.getCreators()==null);
        conditions.add(series.getAccrualPeriodicityCode()==null);
        conditions.add(Objects.equals(series.getPrefLabelLg1(), ""));
        conditions.add(Objects.equals(series.getAccrualPeriodicityCode(), ""));

        Assertions.assertEquals(List.of(false,false,false,false,false), conditions);
    }

    @Test
    void shouldReturnAnExceptionWhenOneParameterIsNotPresentAtLeast() throws  IOException {
        SeriesUtils seriesUtils = new SeriesUtils(true, "fr", "en", repositoryGestion, null, null, famOpeSerIndUtils, null , null, null, null, null);
        String body ="{\n" + "\"id\": \"2025\",\n" + "\"creator\": \"creatorExample\"\n" + "}";
        JSONObject seriesJson = new JSONObject(body);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Series series = mapper.readValue(seriesJson.toString(), Series.class);
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> seriesUtils.verifyBodyToCreateSeries(series));
        assertThat(exception.getDetails()).contains("One or more required parameters are missing.");
    }


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