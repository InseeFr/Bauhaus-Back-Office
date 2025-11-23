package fr.insee.rmes.modules.users.webservice;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.clientside.UserService;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.rbac.ModuleAccessPrivileges;
import fr.insee.rmes.rbac.RbacFetcher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    private final UserService userService;
    private final RbacFetcher rbacService;


    public UserResources(UserService userService, RbacFetcher rbacService) {
        this.userService = userService;
        this.rbacService = rbacService;
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
    public Set<ModuleAccessPrivileges> getUserInformation(@AuthenticationPrincipal Object principal) {
        User user = null;
        try {
            user = this.userService.getUser(principal);
            return rbacService.computePrivileges(user.roles());
        } catch (MissingUserInformationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }

    @GetMapping(value = "/stamp", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "User's stamp", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    public Stamp getStamp(@AuthenticationPrincipal Object principal) {
        try {
            return userService.findStampFrom(principal);
        } catch (MissingUserInformationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }
}