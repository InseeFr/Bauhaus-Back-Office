package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external.services.authentication.stamps.StampsService;
import fr.insee.rmes.rbac.ModuleAccessPrivileges;
import fr.insee.rmes.rbac.RbacFetcher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


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

    private final StampsService stampsService;
    private final RbacFetcher rbacService;
    private final UserDecoder userDecoder;


    public UserResources(StampsService stampsService, RbacFetcher rbacService, UserDecoder userDecoder) {
        this.stampsService = stampsService;
        this.rbacService = rbacService;
        this.userDecoder = userDecoder;
    }

    @GetMapping(
            value = "/info",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Get information about the logged-in user",
            responses = {
                    @ApiResponse(content = @Content(mediaType = "application/json"))
            }
    )
    public Set<ModuleAccessPrivileges> getUserInformation(@AuthenticationPrincipal Object principal) throws RmesException {
        User user = this.userDecoder.fromPrincipal(principal).get();
        return rbacService.computePrivileges(user.roles());
    }

    @GetMapping(value = "/stamp", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "User's stamp", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    public Stamp getStamp(@AuthenticationPrincipal Object principal) throws RmesException {
        return stampsService.findStampFrom(principal);
    }
}