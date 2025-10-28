package fr.insee.rmes.webservice;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.port.clientside.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class StampResources {

    private final OrganisationService organisationService;

    public StampResources(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @GetMapping(value = "/stamps", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List of stamps", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    public ResponseEntity<List<String>> getStamps() throws RmesException {
        return ResponseEntity.ok(organisationService.getStamps());
    }
}