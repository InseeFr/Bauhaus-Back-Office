package fr.insee.rmes.modules.geographies.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeographyResourcesTest {

    @Mock
    GeographyService geoService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    GeographyResources geographyResources;

    @Test
    void shouldReturnLocationHeaderWhenCreateGeography() throws RmesException {
        // Given
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/geo/territory");
        req.setServerName("localhost");
        req.setServerPort(80);
        req.setScheme("http");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        GeographyResources myGeographyResources = new GeographyResources(geoService, objectMapper);
        String expectedIri = "http://bauhaus/qualite/territoire/test-territory-123";
        String expectedId = "test-territory-123";
        when(geoService.createFeature("mocked body")).thenReturn(expectedIri);

        // When
        ResponseEntity<String> response = myGeographyResources.createGeography("mocked body");

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedId, response.getBody());
        assertEquals(
            "/geo/territory/" + expectedId,
            Objects.requireNonNull(response.getHeaders().getLocation()).getPath()
        );
    }

    @Test
    void shouldReturnTerritoriesWithHateoasLinks() throws RmesException, JsonProcessingException {
        // Given
        ObjectMapper realObjectMapper = new ObjectMapper();
        GeographyResources myGeographyResources = new GeographyResources(geoService, realObjectMapper);

        String jsonResult = "[{\"id\":\"territory-1\",\"labelLg1\":\"Territory 1\",\"labelLg2\":\"Territory 1 EN\"},{\"id\":\"territory-2\",\"labelLg1\":\"Territory 2\",\"labelLg2\":\"Territory 2 EN\"}]";

        when(geoService.getGeoFeatures()).thenReturn(jsonResult);

        // When
        ResponseEntity<?> response = myGeographyResources.getGeoFeatures();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody() instanceof List);

        @SuppressWarnings("unchecked")
        List<?> responseList = (List<?>) response.getBody();
        assertEquals(2, responseList.size());
    }

    @Test
    void shouldReturnTerritoryById() throws RmesException {
        // Given
        GeographyResources myGeographyResources = new GeographyResources(geoService, objectMapper);
        org.json.JSONObject jsonObject = new org.json.JSONObject();
        jsonObject.put("id", "territory-1");
        jsonObject.put("labelLg1", "Territory 1");

        when(geoService.getGeoFeatureById("territory-1")).thenReturn(jsonObject);

        // When
        ResponseEntity<Object> response = myGeographyResources.getGeoFeature("territory-1");

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());

        String responseBody = response.getBody().toString();
        Assertions.assertTrue(responseBody.contains("\"id\":\"territory-1\""));
        Assertions.assertTrue(responseBody.contains("\"labelLg1\":\"Territory 1\""));
    }
}
