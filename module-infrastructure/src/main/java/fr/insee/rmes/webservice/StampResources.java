package fr.insee.rmes.webservice;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;
import fr.insee.rmes.domain.port.clientside.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
@Tag(name = "Stamps", description = "Stamps API")
public class StampResources {

    private final OrganisationService organisationService;

    public StampResources(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @GetMapping(value = "/stamps", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List of stamps")
    public ResponseEntity<List<String>> getStamps() throws RmesException {
        return ResponseEntity.ok(organisationService.getStamps());
    }

    @GetMapping(value = "/v2/stamps", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List of stamps")
    public ResponseEntity<List<OrganisationOption>> getOrganisationOptions() throws RmesException {
        return ResponseEntity.ok(organisationService.getOrganisations());
    }
}