package fr.insee.rmes.bauhaus_services.operations.indicators;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@AppSpringBootTest
@ExtendWith(MockitoExtension.class)
class IndicatorsUtilsTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Autowired
    private FamOpeSerIndUtils famOpeSerIndUtils;

    @Test
    void shouldThrowExceptionIfWasGeneratedByNull() throws RmesException {
        JSONObject indicator = new JSONObject();

        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en");
        when(repositoryGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put(Constants.ID, "p1000"));
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> indicatorsUtils.setIndicator(indicator.toString()));
        assertThat(exception.getDetails()).contains("An indicator should be linked to a series.");
    }

    @Test
    void shouldThrowExceptionIfWasGeneratedByEmpty() throws RmesException {
        JSONObject indicator = new JSONObject().put("wasGeneratedBy", new JSONArray());

        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en");
        when(repositoryGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put(Constants.ID, "p1000"));
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> indicatorsUtils.setIndicator(indicator.toString()));
        assertThat(exception.getDetails()).contains("An indicator should be linked to a series.");
    }

    @Test
    void shouldThrowExceptionIfLabelLg1Exist() throws RmesException {
        JSONObject indicator = new JSONObject()
                .put("id", "1")
                .put("wasGeneratedBy", new JSONArray().put(new JSONObject()))
                .put("prefLabelLg1", "prefLabelLg1")
                .put("prefLabelLg2", "prefLabelLg2");

        try (MockedStatic<IndicatorsQueries> mockedFactory = Mockito.mockStatic(IndicatorsQueries.class)) {
            mockedFactory.when(() -> IndicatorsQueries.checkPrefLabelUnicity(eq("p1001"), eq("prefLabelLg1"), eq("fr"))).thenReturn("query");

            when(repositoryGestion.getResponseAsBoolean("query")).thenReturn(true);
            when(repositoryGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put(Constants.ID, "p1000"));

            IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en");
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> indicatorsUtils.setIndicator(indicator.toString()));
            assertThat(exception.getDetails()).contains("This prefLabelLg1 is already used by another indicator.");
        }
    }

    @Test
    void shouldThrowExceptionIfLabelLg2Exist() throws RmesException {
        JSONObject indicator = new JSONObject()
                .put("id", "1")
                .put("wasGeneratedBy", new JSONArray().put(new JSONObject()))
                .put("prefLabelLg1", "prefLabelLg1")
                .put("prefLabelLg2", "prefLabelLg2");

        try (MockedStatic<IndicatorsQueries> mockedFactory = Mockito.mockStatic(IndicatorsQueries.class)) {
            mockedFactory.when(() -> IndicatorsQueries.checkPrefLabelUnicity(eq("p1001"), eq("prefLabelLg1"), eq("fr"))).thenReturn("query1");
            mockedFactory.when(() -> IndicatorsQueries.checkPrefLabelUnicity(eq("p1001"), eq("prefLabelLg2"), eq("en"))).thenReturn("query2");

            when(repositoryGestion.getResponseAsBoolean("query1")).thenReturn(false);
            when(repositoryGestion.getResponseAsBoolean("query2")).thenReturn(true);
            when(repositoryGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put(Constants.ID, "p1000"));

            IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en");
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> indicatorsUtils.setIndicator(indicator.toString()));
            assertThat(exception.getDetails()).contains("This prefLabelLg2 is already used by another indicator.");
        }
    }

    @Test
    void shouldAddAbstractPropertyWithNewSyntaxIfFeatureFlagTrue() throws RmesException {
        doNothing().when(repositoryGestion).deleteObject(any(), any());
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en");

        var indicator = new Indicator();
        indicator.setId("1");
        indicator.setAbstractLg1("AbstractLg1");
        indicator.setAbstractLg2("setAbstractLg2");
        IRI familyIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + indicator.getId());
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
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(false, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en");

        var indicator = new Indicator();
        indicator.setId("1");
        indicator.setAbstractLg1("AbstractLg1");
        indicator.setAbstractLg2("AbstractLg1");
        IRI familyIri = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/" + indicator.getId());
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        indicatorsUtils.addMulltiLangValues(model, familyIri, simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"), "fr", "en", DCTERMS.ABSTRACT);


        Assertions.assertEquals(model.subjects().toArray()[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));

        Assertions.assertEquals(model.predicates().toArray()[0], simpleValueFactory.createIRI(DCTERMS.ABSTRACT.toString()));

        Assertions.assertEquals("\"<p>fr</p>\"@fr", model.objects().toArray()[0].toString());
        Assertions.assertEquals("\"<p>en</p>\"@en", model.objects().toArray()[1].toString());
    }

    @Test
    void givenBuildIndicatorFromJson_whenCorrectRequest_thenResponseIsOk() throws RmesException {
        String json = "{\"idSims\":\"1779\",\"wasGeneratedBy\":[{\"labelLg2\":\"Other indexes\",\"labelLg1\":\"Autres indicateurs\",\"id\":\"s1034\"}],\"abstractLg1\":\"Le nombre d'immatriculations de voitures particulières neuves permet de suivre l'évolution du marché automobile français et constitue l'un des indicateurs permettant de calculer la consommation des ménages en automobile.\",\"prefLabelLg1\":\"Immatriculations de voitures particulières neuves\",\"abstractLg2\":\"The number of new private car registrations is used to monitor the trends on the French automobile market and constitutes one of the indicators used to calculate household automobile consumption.\",\"prefLabelLg2\":\"New private car registrations\",\"creators\":[],\"publishers\":[],\"id\":\"p1638\",\"contributors\":[]}  ";
        JSONObject jsonIndicator = new JSONObject(json);
        Indicator indicator = initIndicator();

        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en");


        Indicator indicatorByApp = indicatorsUtils.buildIndicatorFromJson(jsonIndicator);
        org.assertj.core.api.Assertions.assertThat(indicator).usingRecursiveComparison().isEqualTo(indicatorByApp);

    }


    public Indicator initIndicator() throws RmesException {
        Indicator indicator = new Indicator();
        indicator.setId("p1638");
        indicator.setIdSims("1779");

        indicator.setAbstractLg1("Le nombre d'immatriculations de voitures particulières neuves permet de suivre l'évolution du marché automobile français et constitue l'un des indicateurs permettant de calculer la consommation des ménages en automobile.");
        indicator.setAbstractLg2("The number of new private car registrations is used to monitor the trends on the French automobile market and constitutes one of the indicators used to calculate household automobile consumption.");
        indicator.setPrefLabelLg1("Immatriculations de voitures particulières neuves");
        indicator.setPrefLabelLg2("New private car registrations");

        List<String> creators = new ArrayList<>();
        indicator.setCreators(creators);

        List<OperationsLink> pubList = new ArrayList<>();
        indicator.setPublishers(pubList);

        List<OperationsLink> contrList = new ArrayList<>();
        indicator.setContributors(contrList);

        OperationsLink wgb = new OperationsLink("s1034",null,"Autres indicateurs","Other indexes");
        List<OperationsLink> wgbList = new ArrayList<>();
        wgbList.add(wgb);
        indicator.setWasGeneratedBy(wgbList);
        return indicator;
    }

}