package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.series;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryInitiator;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OpSeriesQueriesTest extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData(){
        container.withTrigFiles("all-operations-and-indicators.trig");
        container.withTrigFiles("sims-all.trig");
    }

    @Test
    void should_return_true_if_series_if_label_exist() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        boolean result = repositoryGestion.getResponseAsBoolean(OpSeriesQueries.checkPrefLabelUnicity("1", "Enquête Loyers et charges", "fr"));
        assertTrue(result);
    }

    @Test
    void should_return_false_series_if_label_does_not_exist() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        boolean result = repositoryGestion.getResponseAsBoolean(OpSeriesQueries.checkPrefLabelUnicity("1", "label", "fr"));
        assertFalse(result);
    }

    @Test
    void should_return_series() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONObject result = repositoryGestion.getResponseAsObject(OpSeriesQueries.oneSeriesQuery("s1226", false));
        assertEquals(result.getString("validationState"), "Validated");
        assertEquals(result.getString("altLabelLg1"), "EVA");
        assertEquals(result.getString("altLabelLg2"), "EVA");
        assertEquals(result.getString("id"), "s1226");
        assertEquals(result.getString("prefLabelLg1"), "Enquête sur l'entrée dans la vie adulte");
        assertEquals(result.getString("prefLabelLg2"), "Survey on entry into adult life");
        assertEquals(result.getString("abstractLg1"), "<p>L&rsquo;objectif du dispositif sur l&rsquo;entr&eacute;e dans la vie adulte (EVA ) est d&rsquo;observer l&rsquo;entr&eacute;e dans la vie adulte et l&rsquo;insertion professionnelle des jeunes, au regard de leurs &eacute;tudes.<br /><br />Il permet de mettre en regard les informations sur les d&eacute;buts de carri&egrave;re et l&rsquo;insertion professionnelle avec les cursus scolaires et universitaires d&eacute;taill&eacute;s et les projets form&eacute;s pendant l&rsquo;adolescence ou la jeunesse.</p>");
        assertEquals(result.getString("abstractLg2"), "<p>The purpose of the EVA system is to observe the entry of young people into adult life and their professional integration in the light of their studies. The EVA system allows to link and compare informations about professional career beginning and professional integration to data related to detailed school and university pathways and plans made for the future during youth ages.</p>");
        assertEquals(result.getString("historyNoteLg1"), "<p>Une premi&egrave;re s&eacute;rie d&rsquo;enqu&ecirc;tes EVA avait &eacute;t&eacute; men&eacute;e par l&rsquo;Insee de 2005 &agrave; 2012 aupr&egrave;s des jeunes du panel de la Depp d&rsquo;&eacute;l&egrave;ves du second degr&eacute; entr&eacute;s en 6e en 1995.<br />Ce dispositif se d&eacute;composait en deux volets&nbsp;: <br />- le parcours scolaire des jeunes dans le secondaire ainsi que leurs &eacute;tudes sup&eacute;rieures sont suivis par la Direction de l'&eacute;valuation, de la prospective et de la performance (Depp - Minist&egrave;re en charge de l&rsquo;&eacute;ducation)&nbsp;;<br />- et les sortants du syst&egrave;me scolaire sont interrog&eacute;s dans le cadre de l&rsquo;enqu&ecirc;te EVA de l&rsquo;Insee.<br /><br />La nouvelle s&eacute;rie d&rsquo;enqu&ecirc;tes EVA s&rsquo;appuie sur l&rsquo;&eacute;dition suivante du panel de la Depp, celle des entrants en 6e en 2007.<br />Apr&egrave;s deux enqu&ecirc;tes l&eacute;g&egrave;res en 2013 et 2014 r&eacute;alis&eacute;es par l&rsquo;Insee, les jeunes sont suivis chaque ann&eacute;e via une enqu&ecirc;te dite de tronc commun.<br />Ce dispositif se d&eacute;compose en trois volets&nbsp;:<br />&nbsp;- le parcours scolaire des jeunes dans le secondaire est suivi par la&nbsp; Direction de l'&eacute;valuation, de la prospective et de la performance (Depp - Minist&egrave;re en charge de l&rsquo;&eacute;ducation)&nbsp;;<br />- la p&eacute;riode des &eacute;tudes sup&eacute;rieures est suivie par le Syst&egrave;me d'information et d'&eacute;tudes statistiques (Sies - Minist&egrave;re en charge de l&rsquo;enseignement sup&eacute;rieur)&nbsp;;<br />- les sortants du syst&egrave;me scolaire sont interrog&eacute;s dans le cadre de l&rsquo;enqu&ecirc;te EVA de l&rsquo;Insee.</p>");
        assertEquals(result.getString("historyNoteLg2"), "<p>A first sequence of EVA surveys had been carried out by Insee from 2005 to 2012 and collected from the young people of the Depp education panel who had entered the first year of secondary education in 1995.<br />This program was consisting of two parts&nbsp;:<br />- the schooling path of young people in secondary education and their higher education studies are observed by the Ministry for Education (Depp)&nbsp;;<br />- and the data collection on young people who have left the education system is conducted by Insee, by the mean of the EVA survey.<br /><br />This new sequence of EVA surveys relies on the following edition of the Depp panel, of young people who entered the first year of secondary education in 2007.<br />After two short surveys in 2013 and 2014 conducted by Insee, the situation of these young persons is observed each year by a survey named &laquo;&nbsp;de tronc commun&nbsp;&raquo;.<br />This program consists of three parts&nbsp;:<br />- the schooling path of young people in secondary education is observed by the Ministry for Education (Depp)&nbsp;;<br />- the&nbsp; higher education studies period is observed by the Ministry in charge of Higher Education (Sies)&nbsp;;<br />- and the data collection on young people who have left the education system is conducted by Insee, by the mean of the EVA survey.</p>");
    }

    @Test
    void should_return_no_series_for_search_if_unknown_stamp() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.getSeriesForSearch("unknow_stamp"));
        assertEquals(0, result.length());
    }

    @Test
    void should_return_one_series_for_search() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.getSeriesForSearch("stamp"));
        assertEquals(1, result.length());
    }

    @Test
    void should_return_all_series_for_search_if_not_defined_stamp() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.getSeriesForSearch(""));
        assertEquals(174, result.length());
    }

    @Test
    void should_return_series_family() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONObject family = repositoryGestion.getResponseAsObject(OpSeriesQueries.getFamily("s1028"));
        assertEquals("Housing", family.getString("labelLg2"));
        assertEquals("Logement", family.getString("labelLg1"));
        assertEquals("s60", family.getString("id"));
    }

    @Test
    void should_return_series_crators() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray creators = repositoryGestion.getResponseAsArray(OpSeriesQueries.getCreatorsBySeriesUri("<http://bauhaus/operations/serie/s1236>"));
        assertEquals("stamp", creators.getJSONObject(0).getString("creators"));
        assertEquals(1, creators.length());
    }

    @Test
    void should_return_true_if_series_has_sims() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        boolean hasSims = repositoryGestion.getResponseAsBoolean(OpSeriesQueries.checkIfSeriesHasSims("http://bauhaus/operations/serie/s1236"));
        assertTrue(hasSims);
    }

    @Test
    void should_return_false_if_series_does_not_have_sims() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        boolean hasSims = repositoryGestion.getResponseAsBoolean(OpSeriesQueries.checkIfSeriesHasSims("http://bauhaus/operations/serie/s12361"));
        assertFalse(hasSims);
    }

    @Test
    void should_return_true_if_series_has_operation() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        boolean hasSims = repositoryGestion.getResponseAsBoolean(OpSeriesQueries.checkIfSeriesHasOperation("http://bauhaus/operations/serie/s1228"));
        assertTrue(hasSims);
    }

    @Test
    void should_return_false_if_series_does_not_have_operation() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        boolean hasSims = repositoryGestion.getResponseAsBoolean(OpSeriesQueries.checkIfSeriesHasOperation("http://bauhaus/operations/serie/s1236"));
        assertFalse(hasSims);
    }

    @Test
    void should_get_creators() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray creators = repositoryGestion.getResponseAsArray(OpSeriesQueries.getCreatorsById("s1236"));
        assertEquals(1, creators.length());
        assertEquals("stamp", creators.getJSONObject(0).getString("creators"));
    }

    @Test
    void should_get_operations() throws RmesException {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray operations = repositoryGestion.getResponseAsArray(OpSeriesQueries.getOperations("s1207"));
        assertEquals(13, operations.length());
    }

    @Test
    void should_return_all_series() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.seriesWithSimsQuery());
        assertEquals(174, result.length());

        for (var i = 0; i < result.length(); i++){
            assertNotNull(result.getJSONObject(i).getString("iri"));
        }
    }

    @Test
    void should_return_all_series_and_operators() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.checkIfSeriesExists(List.of("http://bauhaus/operations/serie/s1028", "http://bauhaus/operations/operation/s1489")));
        assertEquals(2, result.length());
    }

    @Test
    void should_return_filter_missing_objects() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.checkIfSeriesExists(List.of("http://bauhaus/operations/serie/unknown", "http://bauhaus/operations/operation/s1489")));
        assertEquals(1, result.length());
    }

    @Test
    void should_return_published_operations_for_series() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.getPublishedOperationsForSeries("http://bauhaus/operations/serie/s1227"));
        assertEquals(1, result.length());
    }
}
