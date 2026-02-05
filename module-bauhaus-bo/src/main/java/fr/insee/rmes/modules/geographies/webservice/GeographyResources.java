package fr.insee.rmes.modules.geographies.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.geographies.model.GeoFeature;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.geographies.webservice.response.TerritoryResponse;
import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@RestController
@RequestMapping("/geo")
public class GeographyResources {

	final GeographyService geoService;
	final ObjectMapper objectMapper;

	public GeographyResources(GeographyService geoService, ObjectMapper objectMapper) {
		this.geoService = geoService;
		this.objectMapper = objectMapper;
	}


	/***************************************************************************************************
	 * COG
	 ******************************************************************************************************/
	@GetMapping(value = "/territories", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
	@HasAccess(module = RBAC.Module.GEOGRAPHY, privilege = RBAC.Privilege.READ)
	public ResponseEntity<List<TerritoryResponse>> getGeoFeatures() throws RmesException, JsonProcessingException {
		String jsonResultat = geoService.getGeoFeatures();

		// Handle null or empty response
		if (jsonResultat == null || jsonResultat.trim().isEmpty()) {
			return ResponseEntity.ok()
				.contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
				.body(List.of());
		}

		List<GeoFeature> geoFeatures = objectMapper.readValue(jsonResultat, new TypeReference<List<GeoFeature>>() {});

		List<TerritoryResponse> responses = geoFeatures.stream()
			.map(geoFeature -> {
				var response = TerritoryResponse.fromDomain(geoFeature);
				response.add(linkTo(GeographyResources.class).slash("territory").slash(geoFeature.getId()).withSelfRel());
				return response;
			})
			.toList();

		return ResponseEntity.ok()
			.contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
			.body(responses);
	}
	
	@GetMapping(value = "/territory/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasAccess(module = RBAC.Module.GEOGRAPHY, privilege = RBAC.Privilege.READ)
	public ResponseEntity<Object> getGeoFeature(@PathVariable(Constants.ID) String id) throws RmesException {
		String jsonResultat = geoService.getGeoFeatureById(id).toString();
		return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
	}
	

	@HasAccess(module = RBAC.Module.GEOGRAPHY, privilege = RBAC.Privilege.CREATE)
	@PostMapping(value = "/territory", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> createGeography(@RequestBody String body) {
		try {
			String iri = geoService.createFeature(body);

			// Handle null IRI response
			if (iri == null || iri.isEmpty()) {
				return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
					.body("Failed to create geography: no IRI returned");
			}

			// Extract ID from IRI (IRI format: http://.../{id})
			String id = iri.substring(iri.lastIndexOf('/') + 1);

			URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(id)
				.toUri();
			return ResponseEntity.created(location).body(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
	}

	@HasAccess(module = RBAC.Module.GEOGRAPHY, privilege = RBAC.Privilege.UPDATE)
	@PutMapping(value = "/territory/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateGeography(
			@PathVariable(Constants.ID) String id,
			@RequestBody String body) {
		try {
			geoService.updateFeature(id, body);
			return ResponseEntity.ok(HttpStatus.SC_OK);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
	}
}
