package fr.insee.rmes.modules.users.webservice;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.clientside.UserService;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
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
public class UserResources {

    private final UserService userService;


    public UserResources(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(
            value = "/info",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Set<ModuleAccessPrivileges> getUserInformation(@AuthenticationPrincipal Object principal) {
        User user = null;
        try {
            return userService.computePrivileges(principal);
        } catch (MissingUserInformationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }

    @GetMapping(value = "/stamp", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<Stamp> getStamps(@AuthenticationPrincipal Object principal) {
        try {
            return userService.findStampsFrom(principal);
        } catch (MissingUserInformationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }
}