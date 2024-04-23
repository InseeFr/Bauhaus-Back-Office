package fr.insee.rmes.bauhaus_services.geography;

import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.distribution.DistributionServiceImpl;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.model.geography.GeoFeature;
import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class GeographyServiceImplTest {

    @Autowired
    GeographyServiceImpl geographyService;

    @Test
    void shouldReturnNotAcceptableExeptionIfMissingId() {
        GeoFeature feature = new GeoFeature();
        RmesException exception = assertThrows(RmesNotAcceptableException.class, () -> geographyService.createRdfGeoFeature(feature));
        Assertions.assertEquals("{\"code\":845,\"message\":\"No uri found\"}", exception.getDetails());
    }

    @Test
    void shouldReturnNotAcceptableExeptionIfMissingLabelLg1() {
        GeoFeature feature = new GeoFeature();
        feature.setId("id");
        RmesException exception = assertThrows(RmesNotAcceptableException.class, () -> geographyService.createRdfGeoFeature(feature));
        Assertions.assertEquals("{\"code\":846,\"message\":\"LabelLg1 is mandatory\"}", exception.getDetails());
    }

    @Test
    void shouldReturnNotAcceptableExeptionIfMissingLabelLg2() {
        GeoFeature feature = new GeoFeature();
        feature.setId("id");
        feature.setLabelLg1("labelLg1");
        RmesException exception = assertThrows(RmesNotAcceptableException.class, () -> geographyService.createRdfGeoFeature(feature));
        Assertions.assertEquals("{\"code\":846,\"message\":\"LabelLg2 is mandatory\"}", exception.getDetails());
    }
}