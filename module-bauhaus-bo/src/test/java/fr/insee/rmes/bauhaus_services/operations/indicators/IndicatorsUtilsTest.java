package fr.insee.rmes.bauhaus_services.operations.indicators;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;

@AppSpringBootTest
@ExtendWith(MockitoExtension.class)
class IndicatorsUtilsTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private OperationIndicatorsQueries operationIndicatorsQueries;

    @Autowired
    private FamOpeSerIndUtils famOpeSerIndUtils;

    @Test
    void shouldThrowExceptionIfWasGeneratedByNull() throws RmesException {
        JSONObject indicator = new JSONObject();

        IndicatorsUtils indicatorsUtils = spy(new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", null, null));
        doReturn("p1000").when(indicatorsUtils).createID();

        Exception exception = assertThrows(Exception.class, () -> indicatorsUtils.setIndicator(indicator.toString()));
        org.assertj.core.api.Assertions.assertThat(exception)
            .isInstanceOfAny(RmesBadRequestException.class, RmesException.class);
        assertThat(((RmesException) exception).getDetails()).contains("An indicator should be linked to a series.");
    }

    @Test
    void shouldThrowExceptionIfWasGeneratedByEmpty() throws RmesException {
        JSONObject indicator = new JSONObject().put("wasGeneratedBy", new JSONArray());

        IndicatorsUtils indicatorsUtils = spy(new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", null, null));
        doReturn("p1000").when(indicatorsUtils).createID();

        Exception exception = assertThrows(Exception.class, () -> indicatorsUtils.setIndicator(indicator.toString()));
        org.assertj.core.api.Assertions.assertThat(exception)
            .isInstanceOfAny(RmesBadRequestException.class, RmesException.class);
        assertThat(((RmesException) exception).getDetails()).contains("An indicator should be linked to a series.");
    }

    @Test
    void shouldThrowExceptionIfLabelLg1Exist() throws RmesException {
        JSONObject indicator = new JSONObject()
                .put("id", "1")
                .put("wasGeneratedBy", new JSONArray().put(new JSONObject()))
                .put("prefLabelLg1", "prefLabelLg1")
                .put("prefLabelLg2", "prefLabelLg2");

        when(operationIndicatorsQueries.checkPrefLabelUnicity(eq("p1001"), eq("prefLabelLg1"), eq("fr"))).thenReturn("query");
        when(repositoryGestion.getResponseAsBoolean("query")).thenReturn(true);
        when(repositoryGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put(Constants.ID, "p1000"));

        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", operationIndicatorsQueries, null);
        RmesBadRequestException exception = assertThrows(RmesBadRequestException.class, () -> indicatorsUtils.setIndicator(indicator.toString()));
        assertThat(exception.getDetails()).contains("This prefLabelLg1 is already used by another indicator.");
    }

    @Test
    void shouldThrowExceptionIfLabelLg2Exist() throws RmesException {
        JSONObject indicator = new JSONObject()
                .put("id", "1")
                .put("wasGeneratedBy", new JSONArray().put(new JSONObject()))
                .put("prefLabelLg1", "prefLabelLg1")
                .put("prefLabelLg2", "prefLabelLg2");

        when(operationIndicatorsQueries.checkPrefLabelUnicity(eq("p1001"), eq("prefLabelLg1"), eq("fr"))).thenReturn("query1");
        when(operationIndicatorsQueries.checkPrefLabelUnicity(eq("p1001"), eq("prefLabelLg2"), eq("en"))).thenReturn("query2");
        when(repositoryGestion.getResponseAsBoolean("query1")).thenReturn(false);
        when(repositoryGestion.getResponseAsBoolean("query2")).thenReturn(true);
        when(repositoryGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put(Constants.ID, "p1000"));

        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", operationIndicatorsQueries, null);
        RmesBadRequestException exception = assertThrows(RmesBadRequestException.class, () -> indicatorsUtils.setIndicator(indicator.toString()));
        assertThat(exception.getDetails()).contains("This prefLabelLg2 is already used by another indicator.");
    }

    @Test
    void shouldAddAbstractPropertyWithNewSyntaxIfFeatureFlagTrue() throws RmesException {
        doNothing().when(repositoryGestion).deleteObject(any(), any());
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", null, null);

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
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(false, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", null, null);

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

        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", null, null);


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

    @Test
    void createID_returnsP1_whenNoIndicatorExistsInProductsGraph() throws RmesException {
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", operationIndicatorsQueries, null);
        when(operationIndicatorsQueries.lastID()).thenReturn("query");
        when(repositoryGestion.getResponseAsObject("query")).thenReturn(new JSONObject());

        String id = indicatorsUtils.createID();

        assertThat(id).isEqualTo("p1");
    }

    @Test
    void createID_returnsP1_whenLastIdIsUndefined() throws RmesException {
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(true, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", operationIndicatorsQueries, null);
        when(operationIndicatorsQueries.lastID()).thenReturn("query");
        when(repositoryGestion.getResponseAsObject("query")).thenReturn(new JSONObject().put(Constants.ID, Constants.UNDEFINED));

        String id = indicatorsUtils.createID();

        assertThat(id).isEqualTo("p1");
    }

    @Test
    void validate_throwsBadRequest_whenAnOrganisationIsUnknown() throws RmesException {
        OrganisationLookup lookup = mock(OrganisationLookup.class);
        when(lookup.findUnknown(any())).thenReturn(List.of("http://bauhaus/organisations/MISSING"));
        when(repositoryGestion.getResponseAsBoolean(any())).thenReturn(false);
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(false, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", operationIndicatorsQueries, lookup);

        Indicator indicator = new Indicator();
        OperationsLink wgb = new OperationsLink("s1", null, "Series", "Series");
        indicator.setWasGeneratedBy(List.of(wgb));
        indicator.setCreators(List.of("http://bauhaus/organisations/MISSING"));

        Exception exception = assertThrows(RmesBadRequestException.class, () -> indicatorsUtils.validate(indicator));
        assertThat(((RmesException) exception).getDetails()).contains("MISSING");
    }

    private static final IRI TEST_GRAPH = SimpleValueFactory.getInstance().createIRI("http://test/products");

    @Test
    void addCreators_writesEachCreatorAsAnIriTriple() {
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(false, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", null, null);
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI indicURI = vf.createIRI("http://bauhaus/indicators/i1");
        Model model = new LinkedHashModel();

        indicatorsUtils.addCreators(model, indicURI, List.of(
                "http://bauhaus/organisations/DG75-A001",
                "http://bauhaus/organisations/DG75-B002"), TEST_GRAPH);

        IRI dcCreator = vf.createIRI(DC.CREATOR.toString());
        List<Value> creators = model.filter(indicURI, dcCreator, null).stream()
                .map(Statement::getObject)
                .toList();
        assertThat(creators).hasSize(2);
        assertThat(creators.get(0)).isInstanceOf(IRI.class);
        assertThat(creators.get(1)).isInstanceOf(IRI.class);
    }

    @Test
    void addOrganisationLinks_writesIriPassthrough_whenLinkIdIsAlreadyAnIri() throws RmesException {
        OrganisationLookup lookup = mock(OrganisationLookup.class);
        when(lookup.resolve("http://bauhaus/organisations/DG75-A001"))
                .thenReturn(java.util.Optional.of("http://bauhaus/organisations/DG75-A001"));
        IndicatorsUtils indicatorsUtils = new IndicatorsUtils(false, repositoryGestion, null, null, null, famOpeSerIndUtils, null, null, null, "fr", "en", null, lookup);
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI indicURI = vf.createIRI("http://bauhaus/indicators/i1");
        Model model = new LinkedHashModel();
        OperationsLink link = new OperationsLink();
        link.id = "http://bauhaus/organisations/DG75-A001";

        indicatorsUtils.addOrganisationLinks(List.of(link), DCTERMS.CONTRIBUTOR, model, indicURI, TEST_GRAPH);

        IRI contributor = vf.createIRI(DCTERMS.CONTRIBUTOR.toString());
        List<Value> contributors = model.filter(indicURI, contributor, null).stream()
                .map(Statement::getObject)
                .toList();
        assertThat(contributors).hasSize(1);
        assertThat(contributors.get(0)).isInstanceOf(IRI.class);
        assertThat(contributors.get(0).stringValue()).isEqualTo("http://bauhaus/organisations/DG75-A001");
    }

}