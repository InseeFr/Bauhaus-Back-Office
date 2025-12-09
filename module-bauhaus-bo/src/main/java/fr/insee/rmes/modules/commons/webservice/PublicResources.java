package fr.insee.rmes.modules.commons.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.domain.model.DisseminationStatus;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.TreeSet;

@RestController
@RequestMapping("/")
public class PublicResources {

    static final Logger logger = LoggerFactory.getLogger(PublicResources.class);

    private final String env;
    private final String lg2;
    private final String lg1;
    private final String maxLengthScopeNote;
    private final String defaultMailSender;
    private final String defaultContributor;
    private final String appHost;
    private final List<String> activeModules;
    private final List<String> modules;
    private final String version;
    private final List<String> extraMandatoryFields;

    public PublicResources(
                           @Value("${fr.insee.rmes.bauhaus.env}") String env,
                           @Value("${fr.insee.rmes.bauhaus.lg1}") String lg1,
                           @Value("${fr.insee.rmes.bauhaus.lg2}") String lg2,
                           @Value("${fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote}") String maxLengthScopeNote,
                           @Value("${fr.insee.rmes.bauhaus.concepts.defaultMailSender}") String defaultMailSender,
                           @Value("${fr.insee.rmes.bauhaus.concepts.defaultContributor}") String defaultContributor,
                           @Value("${fr.insee.rmes.bauhaus.appHost}") String appHost,
                           @Value("${fr.insee.rmes.bauhaus.activeModules}") List<String> activeModules,
                           @Value("${fr.insee.rmes.bauhaus.modules}") List<String> modules,
                           @Value("${fr.insee.rmes.bauhaus.version}") String version,
                           @Value("${fr.insee.rmes.bauhaus.validation.operation_series}") List<String> extraMandatoryFields) {
        this.env = env;
        this.lg2 = lg2;
        this.lg1 = lg1;
        this.maxLengthScopeNote = maxLengthScopeNote;
        this.defaultMailSender = defaultMailSender;
        this.defaultContributor = defaultContributor;
        this.appHost = appHost;
        this.activeModules = activeModules;
        this.modules = modules;
        this.version = version;
        this.extraMandatoryFields = extraMandatoryFields;
    }

    @GetMapping(value = "/init", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProperties() throws RmesException {
        JSONObject props = new JSONObject();
        try {
            props.put("appHost", this.appHost);
            props.put("defaultContributor", this.defaultContributor);
            props.put("defaultMailSender", this.defaultMailSender);
            props.put("maxLengthScopeNote", this.maxLengthScopeNote);
            props.put("lg1", this.lg1);
            props.put("lg2", this.lg2);
            props.put("authType", this.getAuthType(this.env));
            props.put("activeModules", this.activeModules);
            props.put("modules", this.modules);
            props.put("version", this.version);
            props.put("extraMandatoryFields", this.extraMandatoryFields);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
        }
        return ResponseEntity.ok(props.toString());
    }

    private String getAuthType(String env) {
        if (env.equals("pre-prod") || env.equals("prod") || env.equals("PROD")) return "OpenIDConnectAuth";
        else return "NoAuthImpl";
    }

    @GetMapping(value = "/disseminationStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDisseminationStatus() {
        TreeSet<String> dsList = new TreeSet<>();
        for (DisseminationStatus ds : DisseminationStatus.values()) {
            try {
                dsList.add(new ObjectMapper().writeValueAsString(ds));
            } catch (JsonProcessingException e) {
                return ResponseEntity.status(500).body(e.getMessage());
            }
        }
        return ResponseEntity.ok().body(dsList.toString());
    }
}