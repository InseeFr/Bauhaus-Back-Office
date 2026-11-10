package fr.insee.rmes.modules.organisations.webservice;

import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.utils.XMLUtils;
import java.util.List;
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

    public OrganisationsResources(
            OrganisationsService organisationsService, OrganizationsService organizationsService) {
        this.organisationsService = organisationsService;
        this.organizationsService = organizationsService;
    }

    @GetMapping(
            value = "/organization/{identifier}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Object> getOrganizationByIdentifier(
            @PathVariable("identifier") String identifier, @RequestHeader(required = false) String accept)
            throws RmesException {
        String resultat;
        if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
            resultat = XMLUtils.produceXMLResponse(organizationsService.getOrganization(identifier));
        } else {
            resultat = organizationsService.getOrganizationJsonString(identifier);
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(resultat);
    }

    @GetMapping(
            value = "",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Object> getOrganizations(@RequestHeader(required = false) String accept)
            throws RmesException, OrganisationFetchException {
        if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
            return ResponseEntity.status(HttpStatus.SC_OK)
                    .body(XMLUtils.produceXMLResponse(organizationsService.getOrganizations()));
        }
        logger.info("[OrganizationsResources] Starting fetching organizations");
        List<OrganisationResponse> resultat = organisationsService.getOrganisations().stream()
                .map(OrganisationResponse::fromDomain)
                .toList();
        logger.info("[OrganizationsResources] fetching organizations is now done");
        return ResponseEntity.status(HttpStatus.SC_OK).body(resultat);
    }
}
