package fr.insee.rmes.bauhaus_services.geography;

import static fr.insee.rmes.bauhaus_services.utils.StoredRdfModels.objectsOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.graphdb.ontologies.GEO;
import fr.insee.rmes.graphdb.ontologies.IGEO;
import fr.insee.rmes.modules.geographies.infrastructure.graphdb.GeographyQueries;
import fr.insee.rmes.modules.geographies.model.GeoFeature;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AppSpringBootTest
class GeographyServiceImplTest {

    @Autowired
    GeographyServiceImpl geographyService;

    @MockitoBean
    RepositoryGestion repoGestion;

    @MockitoBean
    GeographyQueries geographyQueries;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void clearTheTerritoriesCache() {
        cacheManager.getCache(GeographyServiceImpl.GEO_FEATURES_CACHE).clear();
    }

    @Test
    void shouldReturnBadRequestExceptionIfMissingId() {
        GeoFeature feature = new GeoFeature();
        RmesException exception =
                assertThrows(RmesBadRequestException.class, () -> geographyService.createRdfGeoFeature(feature));
        Assertions.assertEquals("{\"code\":845,\"message\":\"id is mandatory\"}", exception.getDetails());
    }

    @Test
    void shouldReturnBadRequestExceptionIfMissingLabelLg1() {
        GeoFeature feature = new GeoFeature();
        feature.setId("id");
        RmesException exception =
                assertThrows(RmesBadRequestException.class, () -> geographyService.createRdfGeoFeature(feature));
        Assertions.assertEquals("{\"code\":846,\"message\":\"LabelLg1 is mandatory\"}", exception.getDetails());
    }

    @Test
    void shouldReturnBadRequestExceptionIfMissingLabelLg2() {
        GeoFeature feature = new GeoFeature();
        feature.setId("id");
        feature.setLabelLg1("labelLg1");
        RmesException exception =
                assertThrows(RmesBadRequestException.class, () -> geographyService.createRdfGeoFeature(feature));
        Assertions.assertEquals("{\"code\":846,\"message\":\"LabelLg2 is mandatory\"}", exception.getDetails());
    }

    @Test
    void shouldStoreTheLabelsTheDescriptionsAndTheCompositionOfTheTerritory() throws RmesException {
        when(repoGestion.getResponseAsObject(any())).thenReturn(new JSONObject());

        String iri = geographyService.createRdfGeoFeature(territory());

        Model model = storedModel();
        assertThat(iri).endsWith("/territoire-1");
        assertThat(objectsOf(model, SKOS.PREF_LABEL)).containsExactlyInAnyOrder("label fr", "label en");
        assertThat(objectsOf(model, IGEO.NOM)).containsExactly("label en");
        assertThat(objectsOf(model, DCTERMS.ABSTRACT)).hasSize(2);
        assertThat(objectsOf(model, GEO.UNION)).containsExactly("http://union");
        assertThat(objectsOf(model, GEO.DIFFERENCE)).containsExactly("http://difference");
    }

    /**
     * Deux territoires ne peuvent pas porter le même libellé, sauf s'il s'agit du même territoire :
     * c'est ce que distingue la comparaison des URI.
     */
    @Test
    void shouldRejectATerritoryWhoseLabelIsAlreadyUsedByAnotherOne() throws RmesException {
        when(repoGestion.getResponseAsObject(any()))
                .thenReturn(new JSONObject().put("territory", "http://another-territory"));

        RmesException exception =
                assertThrows(RmesBadRequestException.class, () -> geographyService.createRdfGeoFeature(territory()));

        assertThat(exception.getDetails()).contains("The labelLg1 already exists");
    }

    @Test
    void shouldAcceptATerritoryWhoseLabelIsAlreadyUsedByItself() throws RmesException {
        GeoFeature feature = territory();
        when(repoGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put("territory", feature.getUri()));

        geographyService.createRdfGeoFeature(feature);

        assertThat(storedModel()).isNotEmpty();
    }

    @Test
    void shouldReadTheTerritoryUnderTheUriTheCogGivesToItsCode() throws RmesException {
        when(geographyQueries.getGeoUriIfExists("11")).thenReturn("uri-query");
        when(repoGestion.getResponseAsObject("uri-query"))
                .thenReturn(new JSONObject().put("uri", "http://cog/region/11"));
        when(geographyQueries.getFeatureQuery("http://cog/region/11")).thenReturn("feature-query");
        when(repoGestion.getResponseAsObject("feature-query")).thenReturn(new JSONObject().put("id", "11"));

        JSONObject feature = geographyService.getGeoFeatureById("11");

        assertThat(feature.getString("id")).isEqualTo("11");
    }

    @Test
    void shouldReadTheTerritoryUnderAStatisticalTerritoryUriWhenTheCogIgnoresIt() throws RmesException {
        when(geographyQueries.getGeoUriIfExists("territoire-1")).thenReturn("uri-query");
        when(repoGestion.getResponseAsObject("uri-query")).thenReturn(new JSONObject());
        when(repoGestion.getResponseAsObject(any())).thenReturn(new JSONObject());

        geographyService.getGeoFeatureById("territoire-1");

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(geographyQueries).getFeatureQuery(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).endsWith("/territoire-1");
    }

    /**
     * Les compositions de tous les territoires sont lues en une seule requête : la liste ne doit
     * pas coûter deux requêtes par territoire composé (dizaines de secondes sur la base INSEE).
     */
    @Test
    void shouldReadTheTerritoriesAndAllTheirCompositionsInTwoQueries() throws RmesException {
        stubTerritoriesAndCompositions();

        geographyService.getGeoFeatures();

        verify(repoGestion, times(2)).getResponseAsArray(anyString());
        verify(geographyQueries, never()).getUnionsForAFeatureQuery(anyString());
        verify(geographyQueries, never()).getDifferenceForAFeatureQuery(anyString());
    }

    @Test
    void shouldGiveEachComposedTerritoryItsOwnUnionsAndDifferences() throws RmesException {
        stubTerritoriesAndCompositions();

        JSONArray features = new JSONArray(geographyService.getGeoFeatures());

        JSONObject composed = features.getJSONObject(0);
        assertThat(composed.has("hasComposition")).isFalse();
        assertThat(composed.getJSONArray("unions").toList())
                .containsExactly(Map.of("uri", "http://cog/11", "labelLg1", "Île-de-France", "code", "11"));
        assertThat(composed.getJSONArray("difference").toList())
                .containsExactly(Map.of("uri", "http://cog/75", "labelLg1", "Paris", "code", "75"));

        JSONObject unionOnly = features.getJSONObject(1);
        assertThat(unionOnly.getJSONArray("unions").length()).isEqualTo(1);
        assertThat(unionOnly.getJSONArray("difference").isEmpty()).isTrue();
    }

    @Test
    void shouldLeaveATerritoryWithoutCompositionUntouched() throws RmesException {
        stubTerritoriesAndCompositions();

        JSONObject plain = new JSONArray(geographyService.getGeoFeatures()).getJSONObject(2);

        assertThat(plain.keySet()).containsExactlyInAnyOrder("uri", "labelLg1");
    }

    @Test
    void shouldServeTheTerritoriesFromTheCacheOnTheSecondCall() throws RmesException {
        stubTerritoriesAndCompositions();

        String first = geographyService.getGeoFeatures();
        String second = geographyService.getGeoFeatures();

        assertThat(second).isEqualTo(first);
        verify(repoGestion, times(2)).getResponseAsArray(anyString());
    }

    @Test
    void shouldReadTheTerritoriesAgainAfterATerritoryIsCreated() throws RmesException {
        stubTerritoriesAndCompositions();
        when(repoGestion.getResponseAsObject(any())).thenReturn(new JSONObject());
        geographyService.getGeoFeatures();

        geographyService.createFeature(territory());
        geographyService.getGeoFeatures();

        verify(repoGestion, times(4)).getResponseAsArray(anyString());
    }

    @Test
    void shouldReadTheTerritoriesAgainAfterATerritoryIsUpdated() throws RmesException {
        stubTerritoriesAndCompositions();
        when(repoGestion.getResponseAsObject(any())).thenReturn(new JSONObject());
        geographyService.getGeoFeatures();

        geographyService.updateFeature("territoire-1", territory());
        geographyService.getGeoFeatures();

        verify(repoGestion, times(4)).getResponseAsArray(anyString());
    }

    private void stubTerritoriesAndCompositions() throws RmesException {
        when(geographyQueries.getFeaturesQuery()).thenReturn("features-query");
        when(geographyQueries.getAllUnionsAndDifferencesQuery()).thenReturn("compositions-query");
        when(repoGestion.getResponseAsArray("features-query"))
                .thenReturn(new JSONArray()
                        .put(new JSONObject()
                                .put("uri", "http://ts/1")
                                .put("labelLg1", "Petite couronne")
                                .put("hasComposition", true))
                        .put(new JSONObject()
                                .put("uri", "http://ts/2")
                                .put("labelLg1", "Grand Est élargi")
                                .put("hasComposition", true))
                        .put(new JSONObject().put("uri", "http://cog/84").put("labelLg1", "Auvergne")));
        when(repoGestion.getResponseAsArray("compositions-query"))
                .thenReturn(new JSONArray()
                        .put(composition("http://ts/1", "unions", "http://cog/11", "Île-de-France", "11"))
                        .put(composition("http://ts/1", "difference", "http://cog/75", "Paris", "75"))
                        .put(composition("http://ts/2", "unions", "http://cog/44", "Grand Est", "44")));
    }

    private static JSONObject composition(String feature, String kind, String uri, String label, String code) {
        return new JSONObject()
                .put("feature", feature)
                .put("composition", kind)
                .put("uri", uri)
                .put("labelLg1", label)
                .put("code", code);
    }

    private static GeoFeature territory() {
        GeoFeature union = new GeoFeature();
        union.setUri("http://union");
        GeoFeature difference = new GeoFeature();
        difference.setUri("http://difference");

        GeoFeature feature = new GeoFeature();
        feature.setId("territoire-1");
        feature.setUri("http://territoire-1");
        feature.setLabelLg1("label fr");
        feature.setLabelLg2("label en");
        feature.setDescriptionLg1("description fr");
        feature.setDescriptionLg2("description en");
        feature.setUnions(List.of(union));
        feature.setDifference(List.of(difference));
        return feature;
    }

    private Model storedModel() throws RmesException {
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).loadSimpleObject(any(IRI.class), modelCaptor.capture());
        return modelCaptor.getValue();
    }
}
