package fr.insee.rmes.webservice;

import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external.services.authentication.stamps.StampsService;
import fr.insee.rmes.external.services.rbac.AccessPrivileges;
import fr.insee.rmes.external.services.rbac.RBACService;
import fr.insee.rmes.model.rbac.RBAC;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
public class UserResources {

    static final Logger logger = LoggerFactory.getLogger(UserResources.class);

    private final StampsService stampsService;
    private final RBACService rbacService;
    private final UserDecoder userDecoder;


    public UserResources(StampsService stampsService, RBACService rbacService, UserDecoder userDecoder) {
        this.stampsService = stampsService;
        this.rbacService = rbacService;
        this.userDecoder = userDecoder;
    }

    @GetMapping(
            value = "/info",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "getUserInformation",
            summary = "Get information about the logged-in user",
            responses = {
                    @ApiResponse(content = @Content(mediaType = "application/json"))
            }
    )
    public AccessPrivileges getUserInformation(@AuthenticationPrincipal Object principal) throws RmesException {
        User user = this.userDecoder.fromPrincipal(principal).get();
        return rbacService.computeRbac(user.roles());
    }

    /**
     * @deprecated
     */
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