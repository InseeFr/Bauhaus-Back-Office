package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.commons.security.PublicEndpoint;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.CodeListSummaryResponse;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialPhysicalInstanceResponse;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PhysicalInstanceParentsResponse;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PhysicalInstanceSearchResponse;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.ValidationResponse;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(
    value = "/ddi",
    produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE }
)
public class DdiResources {

    private final DDIService ddiService;
    private final DDI4toDDI3ConverterService ddi4toDdi3ConverterService;
    private final DDI3toDDI4ConverterService ddi3toDdi4ConverterService;
    private final DDIItemConvertService ddiItemConvertService;
    private final UserProvider userProvider;
    private final RbacFetcher rbacFetcher;
    private final UriUtils uriUtils;

    public DdiResources(
        DDIService ddiService,
        DDI4toDDI3ConverterService ddi4toDdi3ConverterService,
        DDI3toDDI4ConverterService ddi3toDdi4ConverterService,
        DDIItemConvertService ddiItemConvertService,
        UserProvider userProvider,
        RbacFetcher rbacFetcher,
        UriUtils uriUtils
    ) {
        this.ddiService = ddiService;
        this.ddi4toDdi3ConverterService = ddi4toDdi3ConverterService;
        this.ddi3toDdi4ConverterService = ddi3toDdi4ConverterService;
        this.ddiItemConvertService = ddiItemConvertService;
        this.userProvider = userProvider;
        this.rbacFetcher = rbacFetcher;
        this.uriUtils = uriUtils;
    }

    @GetMapping("/physical-instance")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<
        List<PartialPhysicalInstanceResponse>
    > getPhysicalInstances() {
        List<PartialPhysicalInstance> instances = resolvePhysicalInstances();

        List<PartialPhysicalInstanceResponse> responses = instances
            .stream()
            .map(instance -> {
                var response = PartialPhysicalInstanceResponse.fromDomain(
                    instance
                );
                response.add(
                    linkTo(DdiResources.class)
                        .slash("physical-instance")
                        .slash(instance.agency())
                        .slash(instance.id())
                        .withSelfRel()
                );
                return response;
            })
            .toList();

        return ResponseEntity.ok()
            .contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
            .body(responses);
    }

    @GetMapping("/physical-instance/search")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<
        List<PhysicalInstanceSearchResponse>
    > searchPhysicalInstances() {
        List<PhysicalInstanceSearchResponse> responses = resolveByReadStampStrategy(
            ddiService::searchPhysicalInstancesFilteredByStamp,
            ddiService::searchPhysicalInstances
        )
            .stream()
            .map(PhysicalInstanceSearchResponse::fromDomain)
            .toList();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responses);
    }

    @GetMapping("/mutualized-codes-list")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<
        List<CodeListSummaryResponse>
    > getMutualizedCodesLists(
        @RequestHeader(
            value = HttpHeaders.CACHE_CONTROL,
            required = false
        ) String cacheControl
    ) {
        if (requestsCacheBypass(cacheControl)) {
            ddiService.evictMutualizedCodesListsCache();
        }
        List<PartialCodesList> codesLists =
            ddiService.getMutualizedCodesLists();

        List<CodeListSummaryResponse> responses = codesLists
            .stream()
            .map(codesList ->
                new CodeListSummaryResponse(
                    codesList.agency(),
                    codesList.id(),
                    codesList.label(),
                    codesList.name(),
                    codesList.versionDate()
                )
            )
            .toList();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responses);
    }

    /**
     * Le client peut forcer un rafraîchissement du cache mutualisé en envoyant l'en-tête HTTP
     * standard {@code Cache-Control: no-cache} (ou {@code no-store}) sur le GET : le cache est alors
     * vidé avant que la liste ne soit recalculée depuis Colectica.
     */
    private static boolean requestsCacheBypass(String cacheControl) {
        if (cacheControl == null) {
            return false;
        }
        String normalized = cacheControl.toLowerCase();
        return (
            normalized.contains("no-cache") || normalized.contains("no-store")
        );
    }

    @GetMapping("/mutualized-codes-list/{agencyId}/{id}")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<Ddi4Response> getMutualizedCodesList(
        @PathVariable String agencyId,
        @PathVariable(Constants.ID) String id
    ) {
        Ddi4Response response = ddiService.getMutualizedCodesList(agencyId, id);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    private List<PartialPhysicalInstance> resolvePhysicalInstances() {
        return resolveByReadStampStrategy(
            ddiService::getPhysicalInstancesFilteredByStamp,
            ddiService::getPhysicalInstances
        );
    }

    /**
     * Applique la stratégie READ de DDI_PHYSICALINSTANCE : si elle vaut STAMP,
     * renvoie la liste filtrée par les stamps de l'utilisateur courant ; sinon
     * (ou en cas d'information utilisateur manquante) la liste complète.
     */
    private <T> List<T> resolveByReadStampStrategy(
        Function<Set<String>, List<T>> filteredByStamp,
        Supplier<List<T>> unfiltered
    ) {
        try {
            User user = userProvider.findUser().orElse(User.EMPTY_USER);
            RBAC.Strategy strategy =
                rbacFetcher.getApplicationActionStrategyByRole(
                    user.roles(),
                    RBAC.Module.DDI_PHYSICALINSTANCE,
                    RBAC.Privilege.READ
                );
            if (strategy == RBAC.Strategy.STAMP) {
                return filteredByStamp.apply(user.getStamps());
            }
        } catch (MissingUserInformationException | RmesException e) {
            // fall through to unfiltered
        }
        return unfiltered.get();
    }

    @PostMapping("/physical-instance")
    @PreAuthorize(
        "@propertiesAccessPrivilegesChecker.hasAccess('DDI_PHYSICALINSTANCE', 'CREATE', #request.groupAgency + '|' + #request.groupId, authentication.principal)"
    )
    public ResponseEntity<Ddi4Response> createPhysicalInstance(
        @RequestBody CreatePhysicalInstanceRequest request
    ) {
        Ddi4Response response = ddiService.createPhysicalInstance(request);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @GetMapping("/physical-instance/{agencyId}/{id}")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<Ddi4Response> getDdi4PhysicalInstance(
        @PathVariable String agencyId,
        @PathVariable(Constants.ID) String id
    ) {
        Ddi4Response response = ddiService.getDdi4PhysicalInstance(
            agencyId,
            id
        );
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @GetMapping("/physical-instance/{agencyId}/{id}/parents")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<PhysicalInstanceParentsResponse> getPhysicalInstanceParents(
        @PathVariable String agencyId,
        @PathVariable(Constants.ID) String id
    ) {
        PhysicalInstanceParentsResponse response =
            PhysicalInstanceParentsResponse.fromDomain(
                ddiService.getPhysicalInstanceParents(agencyId, id)
            );
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @GetMapping("/physical-instance/{agencyId}/{id}/codeslists")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<
        List<CodeListSummaryResponse>
    > getPhysicalInstanceCodesLists(
        @PathVariable String agencyId,
        @PathVariable(Constants.ID) String id
    ) {
        List<CodeListSummaryResponse> codeLists = ddiService
            .getPhysicalInstanceCodeLists(agencyId, id)
            .stream()
            .map(CodeListSummaryResponse::fromDdi4CodeList)
            .toList();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(codeLists);
    }

    @PatchMapping("/physical-instance/{agencyId}/{id}")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.UPDATE
    )
    public ResponseEntity<Ddi4Response> updatePhysicalInstance(
        @PathVariable String agencyId,
        @PathVariable String id,
        @RequestBody UpdatePhysicalInstanceRequest request
    ) {
        Ddi4Response updatedInstance = ddiService.updatePhysicalInstance(
            agencyId,
            id,
            request
        );
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(updatedInstance);
    }

    @PutMapping("/physical-instance/{agencyId}/{id}")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.UPDATE
    )
    public ResponseEntity<Ddi4Response> replacePhysicalInstance(
        @PathVariable String agencyId,
        @PathVariable String id,
        @RequestBody Ddi4Response ddi4Response
    ) {
        Ddi4Response updatedInstance = ddiService.updateFullPhysicalInstance(
            agencyId,
            id,
            ddi4Response
        );
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(updatedInstance);
    }

    @PostMapping("/convert/ddi4-to-ddi3")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<String> convertDdi4ToDdi3(
        @RequestBody Ddi4Response ddi4
    ) {
        String ddi3Xml = ddi4toDdi3ConverterService.convertDdi4ToDdi3Xml(ddi4);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body(ddi3Xml);
    }

    @PostMapping("/convert/ddi3-to-ddi4")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<Ddi4Response> convertDdi3ToDdi4(
        @RequestBody Ddi3Response ddi3
    ) {
        String schemaUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/ddi/schema")
            .toUriString();
        Ddi4Response ddi4 = ddi3toDdi4ConverterService.convertDdi3ToDdi4(
            ddi3,
            schemaUrl
        );
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(ddi4);
    }

    @GetMapping("/schema")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<String> getDdiSchema() throws IOException {
        ClassPathResource resource = new ClassPathResource("ddi-schema.json");
        String schema;
        try (InputStream is = resource.getInputStream()) {
            schema = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        // Remove BOM if present
        if (schema.startsWith("\uFEFF")) {
            schema = schema.substring(1);
        }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(schema);
    }

    @GetMapping(
        value = "/public/item/{agency}/{id}/{version}",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    @PublicEndpoint
    public ResponseEntity<String> getItemXmlByVersion(
        @PathVariable String agency,
        @PathVariable String id,
        @PathVariable String version
    ) {
        String xml = ddiService.getItemXml(agency, id, version);
        if (xml == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body(xml);
    }

    @GetMapping(
        value = "/public/item/{agency}/{id}/{version}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PublicEndpoint
    public ResponseEntity<String> getItemJsonByVersion(
        @PathVariable String agency,
        @PathVariable String id,
        @PathVariable String version
    ) {
        String xml = ddiService.getItemXml(agency, id, version);
        if (xml == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(ddiItemConvertService.convert(xml).toString());
    }

    @GetMapping(
        value = "/public/item/{agency}/{id}",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    @PublicEndpoint
    public ResponseEntity<String> getItemXml(
        @PathVariable String agency,
        @PathVariable String id
    ) {
        String xml = ddiService.getItemXml(agency, id);
        if (xml == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body(xml);
    }

    @GetMapping(
        value = "/public/item/{agency}/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PublicEndpoint
    public ResponseEntity<String> getItemJson(
        @PathVariable String agency,
        @PathVariable String id
    ) {
        String xml = ddiService.getItemXml(agency, id);
        if (xml == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(ddiItemConvertService.convert(xml).toString());
    }

    /**
     * Endpoint #485 : {@code GET /ddi/codelist/{agency}/{id}[/{version}]} renvoie une CodeList et ses
     * Categories référencées, en DDI 3.3 XML (multi-fragments {@code <FragmentInstance>}) ou DDI 4 JSON
     * selon la négociation de contenu. Le préfixe {@code /ddi} est requis par la redirection Gravitee,
     * qui ne route vers Bauhaus que les chemins commençant par {@code /ddi/}.
     */
    @GetMapping(
        value = "/public/codelist/{agency}/{id}/{version}",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    @PublicEndpoint
    public ResponseEntity<String> getCodeListXmlByVersion(
        @PathVariable String agency,
        @PathVariable String id,
        @PathVariable String version
    ) {
        return DdiResponses.xml(ddiService.getCodeListXml(agency, id, version));
    }

    @GetMapping(
        value = "/public/codelist/{agency}/{id}/{version}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PublicEndpoint
    public ResponseEntity<Ddi4Response> getCodeListJsonByVersion(
        @PathVariable String agency,
        @PathVariable String id,
        @PathVariable String version
    ) {
        return DdiResponses.json(ddiService.getCodeList(agency, id, version));
    }

    @GetMapping(
        value = "/public/codelist/{agency}/{id}",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    @PublicEndpoint
    public ResponseEntity<String> getCodeListXml(
        @PathVariable String agency,
        @PathVariable String id
    ) {
        return DdiResponses.xml(ddiService.getCodeListXml(agency, id, null));
    }

    @GetMapping(
        value = "/public/codelist/{agency}/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PublicEndpoint
    public ResponseEntity<Ddi4Response> getCodeListJson(
        @PathVariable String agency,
        @PathVariable String id
    ) {
        return DdiResponses.json(ddiService.getCodeList(agency, id, null));
    }

    /**
     * Endpoint #496 : {@code GET /ddi/operation/{id}/studyUnit} renvoie le StudyUnit d'une opération
     * en DDI 3.3 XML ou DDI 4 JSON selon la négociation de contenu (en-tête {@code Accept}), de manière
     * cohérente avec les autres services DDI ({@code /ddi/item}, {@code /ddi/codelist}).
     */
    @GetMapping(
        value = "/public/operation/{id}/studyUnit",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PublicEndpoint
    public ResponseEntity<String> getOperationStudyUnitJson(
        @PathVariable(Constants.ID) String id
    ) {
        String operationIri = uriUtils.getCompleteUriPublication(
            "operation",
            id
        );

        return ddiService
            .getStudyUnitXmlByOperationIri(operationIri)
            .map(xml ->
                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ddiItemConvertService.convert(xml).toString())
            )
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(
        value = "/public/operation/{id}/studyUnit",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    @PublicEndpoint
    public ResponseEntity<String> getOperationStudyUnitXml(
        @PathVariable(Constants.ID) String id
    ) {
        String operationIri = uriUtils.getCompleteUriPublication(
            "operation",
            id
        );
        return DdiResponses.xml(
            ddiService.getStudyUnitXmlByOperationIri(operationIri).orElse(null)
        );
    }

    @PostMapping("/validate")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.PUBLISH
    )
    public ResponseEntity<ValidationResponse> validateDdi4(
        @RequestBody String jsonData
    ) {
        try {
            // Load schema
            ClassPathResource resource = new ClassPathResource(
                "ddi-schema.json"
            );
            String schemaContent;
            try (InputStream is = resource.getInputStream()) {
                schemaContent = new String(
                    is.readAllBytes(),
                    StandardCharsets.UTF_8
                );
            }
            // Remove BOM if present
            if (schemaContent.startsWith("\uFEFF")) {
                schemaContent = schemaContent.substring(1);
            }

            // Create schema factory and parse schema
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(
                SpecVersion.VersionFlag.V202012
            );
            ObjectMapper mapper = new ObjectMapper();
            JsonNode schemaNode = mapper.readTree(schemaContent);
            JsonSchema schema = factory.getSchema(schemaNode);

            // Parse and validate JSON data. Le DDI 4 circule déjà sous l'enveloppe du schéma
            // ({topLevelReferences, items}) : rien à traduire ici.
            JsonNode jsonNode = mapper.readTree(jsonData);
            Set<ValidationMessage> validationMessages = schema.validate(
                jsonNode
            );

            if (validationMessages.isEmpty()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ValidationResponse.success());
            } else {
                List<String> errors = validationMessages
                    .stream()
                    .map(ValidationMessage::getMessage)
                    .toList();
                return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ValidationResponse.failure(errors));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    ValidationResponse.failure(
                        List.of("Invalid JSON: " + e.getMessage())
                    )
                );
        }
    }
}
