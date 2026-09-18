package fr.insee.rmes.bauhaus_services.geography;

import static fr.insee.rmes.bauhaus_services.utils.StoredRdfModels.objectsOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AppSpringBootTest
class GeographyServiceImplTest {

    @Autowired
    GeographyServiceImpl geographyService;

    @MockitoBean
    RepositoryGestion repoGestion;

    @MockitoBean
    GeographyQueries geographyQueries;

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
