package fr.insee.rmes.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.config.auth.AuthType;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.roles.UserRolesManagerService;
import fr.insee.rmes.config.swagger.model.LabelUrl;
import fr.insee.rmes.config.swagger.model.application.Init;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.authentication.stamps.StampsService;
import fr.insee.rmes.model.dissemination_status.DisseminationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * WebService class for resources
 * schemes: - http
 * <p>
 * consumes: - application/json
 * <p>
 * produces: - application/json
 */
@RestController
@RequestMapping("/")
@Tag(name = "Application", description = "Application API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class PublicResources extends GenericResources {

    static final Logger logger = LoggerFactory.getLogger(PublicResources.class);

    private final UserRolesManagerService userRolesManagerService;

    private final StampsService stampsService;
    private final  String documentsStorageGestion;
    private  final String env;
    private final  String lg2;
    private final  String lg1;
    private final  String maxLengthScopeNote;
    private  final String defaultMailSender;
    private final String defaultContributor;
    private final String sugoiUi;
    private final String appHost;

    public PublicResources(@Autowired UserRolesManagerService userRolesManagerService,
                           @Autowired StampsService stampsService,
                           @Value("${fr.insee.rmes.bauhaus.storage.document.gestion}") String documentsStorageGestion,
                           @Value("${fr.insee.rmes.bauhaus.env}") String env,
                           @Value("${fr.insee.rmes.bauhaus.lg1}") String lg2,
                           @Value("${fr.insee.rmes.bauhaus.lg2}") String lg1,
                           @Value("${fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote}") String maxLengthScopeNote,
                           @Value("${fr.insee.rmes.bauhaus.concepts.defaultMailSender}") String defaultMailSender,
                           @Value("${fr.insee.rmes.bauhaus.concepts.defaultContributor}") String defaultContributor,
                           @Value("${fr.insee.rmes.bauhaus.sugoi.ui}") String sugoiUi,
                           @Value("${fr.insee.rmes.bauhaus.appHost}") String appHost) {
        this.userRolesManagerService = userRolesManagerService;
        this.stampsService = stampsService;
        this.documentsStorageGestion = documentsStorageGestion;
        this.env = env;
        this.lg2 = lg2;
        this.lg1 = lg1;
        this.maxLengthScopeNote = maxLengthScopeNote;
        this.defaultMailSender = defaultMailSender;
        this.defaultContributor = defaultContributor;
        this.sugoiUi = sugoiUi;
        this.appHost = appHost;
    }

    @GetMapping(value = "/init", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getInit", summary = "Initial properties", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Init.class)))})
    public ResponseEntity<Object> getProperties() throws RmesException {
        JSONObject props = new JSONObject();
        try {
            props.put("appHost", this.appHost);
            props.put("authorizationHost", this.sugoiUi);
            props.put("defaultContributor", this.defaultContributor);
            props.put("defaultMailSender", this.defaultMailSender);
            props.put("maxLengthScopeNote", this.maxLengthScopeNote);
            props.put("lg1", this.lg1);
            props.put("lg2", this.lg2);
            props.put("authType", AuthType.getAuthType(this.env));
            props.put("modules", getActiveModules());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(props.toString());
    }

    @GetMapping(value = "/stamps", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getStamps", summary = "List of stamps", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    public ResponseEntity<Object> getStamps() {
        try {
            return ResponseEntity.status(HttpStatus.SC_OK).body(stampsService.getStamps());
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    private List<String> getActiveModules() {
        String dirPath = this.documentsStorageGestion + "/BauhausActiveModules.txt";
        File file = new File(dirPath);
        try {
            return FileUtils.readLines(file, StandardCharsets.UTF_8);//Read lines in a list
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @GetMapping(value = "/disseminationStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getDisseminationStatus", summary = "List of dissemination status", responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = LabelUrl.class))))})
    public ResponseEntity<Object> getDisseminationStatus() {
        TreeSet<String> dsList = new TreeSet<>();
        for (DisseminationStatus ds : DisseminationStatus.values()) {
            try {
                dsList.add(new ObjectMapper().writeValueAsString(ds));
            } catch (JsonProcessingException e) {
                return ResponseEntity.status(500).body(e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(dsList.toString());
    }

    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getRoles", summary = "List of roles", responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Roles.class))))})
    public ResponseEntity<Object> getRoles() {
        String entity = null;
        try {
            entity = userRolesManagerService.getRoles();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(entity);
    }

}