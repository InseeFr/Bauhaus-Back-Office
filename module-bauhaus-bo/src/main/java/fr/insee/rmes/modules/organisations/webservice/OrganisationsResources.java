package fr.insee.rmes.modules.organisations.webservice;

import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.utils.XMLUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
public class OrganisationsResources {


	static final Logger logger = LoggerFactory.getLogger(OrganisationsResources.class);

    final OrganisationsService organisationsService;
	final OrganizationsService organizationsService;

	public OrganisationsResources(OrganisationsService organisationsService, OrganizationsService organizationsService) {
        this.organisationsService = organisationsService;
        this.organizationsService = organizationsService;
	}

	@GetMapping(value = "/organization/{identifier}", 
			produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Object> getOrganizationByIdentifier(
            @PathVariable("identifier") String identifier,
			@RequestHeader(required=false) String accept) {
		String resultat;
		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			try {
				resultat=XMLUtils.produceXMLResponse(organizationsService.getOrganization(identifier));
			} catch (RmesException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getDetails());
			}
		}
		else try {
			resultat = organizationsService.getOrganizationJsonString(identifier);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.SC_OK).body(resultat);
	}

	@GetMapping(value = "", 
			produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Object> getOrganizations(@RequestHeader(required=false) String accept) {
		String resultat;

        if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			try {
				resultat=XMLUtils.produceXMLResponse(organizationsService.getOrganizations());
			} catch (RmesException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getDetails());
			}
		}
		else try {
			logger.info("[OrganizationsResources] Starting fetching organizations");
			resultat = organizationsService.getOrganizationsJson();
			logger.info("[OrganizationsResources] fetching organizations is now done");
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.SC_OK).body(resultat);
	}

}
