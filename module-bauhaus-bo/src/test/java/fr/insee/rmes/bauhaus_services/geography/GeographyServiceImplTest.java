package fr.insee.rmes.bauhaus_services.geography;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.exceptions.RmesBadRequestException;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/bauhaus_services/geography/GeographyServiceImplTest.java
import fr.insee.rmes.onion.domain.exceptions.RmesException;
=======
>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/test/java/fr/insee/rmes/bauhaus_services/geography/GeographyServiceImplTest.java
import fr.insee.rmes.model.geography.GeoFeature;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;

@AppSpringBootTest
class GeographyServiceImplTest {

    @Autowired
    GeographyServiceImpl geographyService;

    @Test
    void shouldReturnBadRequestExceptionIfMissingId() {
        GeoFeature feature = new GeoFeature();
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> geographyService.createRdfGeoFeature(feature));
        Assertions.assertEquals("{\"code\":845,\"message\":\"id is mandatory\"}", exception.getDetails());
    }

    @Test
    void shouldReturnBadRequestExceptionIfMissingLabelLg1() {
        GeoFeature feature = new GeoFeature();
        feature.setId("id");
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> geographyService.createRdfGeoFeature(feature));
        Assertions.assertEquals("{\"code\":846,\"message\":\"LabelLg1 is mandatory\"}", exception.getDetails());
    }

    @Test
    void shouldReturnBadRequestExceptionIfMissingLabelLg2() {
        GeoFeature feature = new GeoFeature();
        feature.setId("id");
        feature.setLabelLg1("labelLg1");
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> geographyService.createRdfGeoFeature(feature));
        Assertions.assertEquals("{\"code\":846,\"message\":\"LabelLg2 is mandatory\"}", exception.getDetails());
    }
}