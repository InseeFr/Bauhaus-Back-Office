package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.DenyListFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for administrative operations on Colectica configuration.
 *
 * <p>This controller provides endpoints for monitoring and debugging the
 * code list deny list configuration and cache status.
 *
 * <p><b>Security Note:</b> These endpoints should be secured in production
 * environments to prevent unauthorized access to configuration details.
 *
 * @see ColecticaConfiguration
 * @see DenyListFilter
 */
@RestController
@RequestMapping("/api/admin/colectica")
@Tag(name = "Colectica Administration", description = "Administrative endpoints for Colectica configuration")
public class ColecticaAdminController {

    private final ColecticaConfiguration configuration;
    private final DenyListFilter denyListFilter;

    /**
     * Creates a new ColecticaAdminController.
     *
     * @param configuration The Colectica configuration
     * @param denyListFilter The deny list filter component
     */
    public ColecticaAdminController(
            ColecticaConfiguration configuration,
            DenyListFilter denyListFilter) {
        this.configuration = configuration;
        this.denyListFilter = denyListFilter;
    }

    /**
     * Returns the current code list deny list configuration.
     *
     * @return The list of deny list entries
     */
    @GetMapping(value = "/deny-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get deny list configuration",
            description = "Returns all configured deny list entries for code lists"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved deny list",
                    content = @Content(schema = @Schema(implementation = ColecticaConfiguration.CodeListDenyEntry.class))),
            @ApiResponse(responseCode = "404", description = "Deny list not configured")
    })
    public ResponseEntity<List<ColecticaConfiguration.CodeListDenyEntry>> getDenyList() {
        List<ColecticaConfiguration.CodeListDenyEntry> denyList = configuration.codeListDenyList();

        if (denyList == null || denyList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(denyList);
    }

    /**
     * Checks if a specific code list is in the deny list.
     *
     * @param agencyId The agency ID to check
     * @param id The code list ID to check
     * @return A map containing the check result and details
     */
    @GetMapping(value = "/deny-list/check", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Check if item is in deny list",
            description = "Verifies whether a specific agencyId/id combination is configured in the deny list"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully performed check"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<Map<String, Object>> isInDenyList(
            @Parameter(description = "Agency ID of the code list", required = true)
            @RequestParam String agencyId,
            @Parameter(description = "ID of the code list", required = true)
            @RequestParam String id) {

        if (agencyId == null || agencyId.isBlank() || id == null || id.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        boolean isDenied = denyListFilter.isInDenyList(agencyId, id);

        Map<String, Object> response = new HashMap<>();
        response.put("agencyId", agencyId);
        response.put("id", id);
        response.put("isInDenyList", isDenied);
        response.put("willBeFiltered", isDenied);

        return ResponseEntity.ok(response);
    }

    /**
     * Returns statistics about the deny list cache.
     *
     * @return A map containing cache statistics
     */
    @GetMapping(value = "/deny-list/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get deny list statistics",
            description = "Returns information about the deny list cache size and configuration"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    public ResponseEntity<Map<String, Object>> getDenyListStats() {
        Map<String, Object> stats = new HashMap<>();

        List<ColecticaConfiguration.CodeListDenyEntry> denyList = configuration.codeListDenyList();
        int configuredEntries = (denyList != null) ? denyList.size() : 0;
        int cacheSize = denyListFilter.getCacheSize();

        stats.put("configuredEntries", configuredEntries);
        stats.put("cacheSize", cacheSize);
        stats.put("cacheInitialized", cacheSize > 0);
        stats.put("denyListActive", configuredEntries > 0);

        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint for the deny list configuration.
     *
     * @return Health status information
     */
    @GetMapping(value = "/deny-list/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Health check for deny list",
            description = "Returns health status of the deny list configuration"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, Object>> getDenyListHealth() {
        Map<String, Object> health = new HashMap<>();

        boolean isConfigured = configuration != null &&
                configuration.codeListDenyList() != null &&
                !configuration.codeListDenyList().isEmpty();

        health.put("status", "UP");
        health.put("denyListConfigured", isConfigured);
        health.put("filterComponentAvailable", denyListFilter != null);

        return ResponseEntity.ok(health);
    }
}
