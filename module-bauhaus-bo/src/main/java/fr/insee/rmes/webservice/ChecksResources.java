package fr.insee.rmes.webservice;

import fr.insee.rmes.domain.model.checks.CheckResult;
import fr.insee.rmes.domain.port.clientside.CheckerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        value = "/checks",
        produces = {
                MediaType.APPLICATION_JSON_VALUE
        }
)
public class ChecksResources {

    private static final Logger logger = LoggerFactory.getLogger(ChecksResources.class);

    private final CheckerService checkerService;

    public ChecksResources(CheckerService checkerService) {
        this.checkerService = checkerService;
    }

    @GetMapping()
    public ResponseEntity<List<CheckResult>> runAllChecks() {
        try {
            logger.info("Starting all data checks");
            List<CheckResult> results = checkerService.checks();
            logger.info("Completed {} checks", results.size());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(results);
        } catch (Exception e) {
            logger.error("Error during checks execution", e);
            // Return a CheckResult with error information instead of empty response
            CheckResult errorResult = new CheckResult("system_error", 
                    "Failed to execute checks: " + e.getMessage());
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(errorResult));
        }
    }
}