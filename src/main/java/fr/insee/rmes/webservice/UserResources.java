package fr.insee.rmes.webservice;

import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.external_services.authentication.stamps.StampsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WebService class for resources of Concepts
 *
 * @author N. Laval
 * <p>
 * schemes: - http
 * <p>
 * consumes: - application/json
 * <p>
 * produces: - application/json
 */
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User", description = "User Management")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class UserResources extends GenericResources {

    static final Logger logger = LoggerFactory.getLogger(UserResources.class);

    private final StampsService stampsService;


    @Autowired
    public UserResources(StampsService stampsService) {
        this.stampsService = stampsService;
    }

    @GetMapping(value = "/stamp",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getStamp", summary = "User's stamp", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    public ResponseEntity<Object> getStamp(@AuthenticationPrincipal Object principal) {
        Stamp stamp;
        try {
            stamp = stampsService.findStampFrom(principal);
        } catch (Exception e) {
            logger.error("exception while retrieving stamp", e);
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("exception while retrieving stamp");
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(stamp);
    }


}