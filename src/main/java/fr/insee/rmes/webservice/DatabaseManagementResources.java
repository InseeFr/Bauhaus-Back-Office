package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.DatabaseManagementService;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Qualifier("Database Management")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/database-management",  produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
public class DatabaseManagementResources {

    @Autowired
    DatabaseManagementService databaseManagementService;


    @PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
    @GetMapping("/reset-graph")
    @io.swagger.v3.oas.annotations.Operation(operationId = "resetGraph", summary = "Reset the graph")
    public ResponseEntity<? extends Object> reset() {
        try {
            databaseManagementService.clearGraph();
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

}
