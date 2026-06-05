package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import fr.insee.rmes.Constants;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialGroupResponse;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for DDI Group operations: the plain {@code /ddi/groups} list and create endpoints,
 * the HATEOAS {@code /ddi/group} browse list (stamp-filtered), and the {@code /ddi/group/{agencyId}/{id}}
 * detail.
 */
@RestController
@RequestMapping(
    value = "/ddi",
    produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE }
)
@ConditionalOnModule("ddi")
public class GroupResources {

    private static final Logger logger = LoggerFactory.getLogger(
        GroupResources.class
    );

    private final GroupService groupService;
    private final DDIService ddiService;
    private final UserProvider userProvider;
    private final RbacFetcher rbacFetcher;

    public GroupResources(
        GroupService groupService,
        DDIService ddiService,
        UserProvider userProvider,
        RbacFetcher rbacFetcher
    ) {
        this.groupService = groupService;
        this.ddiService = ddiService;
        this.userProvider = userProvider;
        this.rbacFetcher = rbacFetcher;
    }

    @GetMapping("/groups")
    public ResponseEntity<List<PartialGroup>> getGroups() {
        logger.info("GET /ddi/groups - Getting all groups");
        try {
            List<PartialGroup> groups = groupService.getAll();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            logger.error("Failed to get groups", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/groups", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createOrUpdateGroup(
        @RequestBody Ddi4Group group
    ) {
        logger.info(
            "POST /ddi/groups - Creating/updating group: id={}",
            group.id()
        );
        try {
            groupService.createOrUpdate(group);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            logger.error("Failed to create/update group: id={}", group.id(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/group")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<List<PartialGroupResponse>> getGroupResponses() {
        List<PartialGroup> groups = resolveGroups();

        List<PartialGroupResponse> responses = groups
            .stream()
            .map(group -> {
                var response = PartialGroupResponse.fromDomain(group);
                response.add(
                    linkTo(GroupResources.class)
                        .slash("group")
                        .slash(group.agency())
                        .slash(group.id())
                        .withSelfRel()
                );
                return response;
            })
            .toList();

        return ResponseEntity.ok()
            .contentType(MediaTypes.HAL_JSON)
            .body(responses);
    }

    @GetMapping("/group/{agencyId}/{id}")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<Ddi4GroupResponse> getDdi4Group(
        @PathVariable String agencyId,
        @PathVariable(Constants.ID) String id
    ) {
        Ddi4GroupResponse response = ddiService.getDdi4Group(agencyId, id);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @GetMapping("/groups/{agencyId}/{id}/logical-products")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<List<PartialLogicalProduct>> getGroupLogicalProducts(
        @PathVariable String agencyId,
        @PathVariable String id
    ) {
        logger.info(
            "GET /ddi/groups/{}/{}/logical-products - Getting logical products of group",
            agencyId,
            id
        );
        try {
            List<PartialLogicalProduct> logicalProducts =
                ddiService.getLogicalProductsByGroup(agencyId, id);
            return ResponseEntity.ok(logicalProducts);
        } catch (Exception e) {
            logger.error(
                "Failed to get logical products for group: agencyId={}, id={}",
                agencyId,
                id,
                e
            );
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(
        "/groups/{groupAgencyId}/{groupId}/logical-products/{logicalProductAgencyId}/{logicalProductId}/code-list-scheme"
    )
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<
        List<PartialCodeListScheme>
    > getLogicalProductCodeListSchemes(
        @PathVariable String groupAgencyId,
        @PathVariable String groupId,
        @PathVariable String logicalProductAgencyId,
        @PathVariable String logicalProductId
    ) {
        logger.info(
            "GET /ddi/groups/{}/{}/logical-products/{}/{}/code-list-scheme - Getting code list schemes of logical product",
            groupAgencyId,
            groupId,
            logicalProductAgencyId,
            logicalProductId
        );
        try {
            List<PartialCodeListScheme> codeListSchemes =
                ddiService.getCodeListSchemesByLogicalProduct(
                    logicalProductAgencyId,
                    logicalProductId
                );
            return ResponseEntity.ok(codeListSchemes);
        } catch (Exception e) {
            logger.error(
                "Failed to get code list schemes for logical product: agencyId={}, logicalProductId={}",
                logicalProductAgencyId,
                logicalProductId,
                e
            );
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(
        "/groups/{groupAgencyId}/{groupId}/logical-products/{logicalProductAgencyId}/{logicalProductId}" +
            "/code-list-scheme/{codeListSchemeAgencyId}/{codeListSchemeId}/codes-list"
    )
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<List<PartialCodesList>> getCodeListSchemeCodesLists(
        @PathVariable String groupAgencyId,
        @PathVariable String groupId,
        @PathVariable String logicalProductAgencyId,
        @PathVariable String logicalProductId,
        @PathVariable String codeListSchemeAgencyId,
        @PathVariable String codeListSchemeId
    ) {
        logger.info(
            "GET /ddi/groups/{}/{}/logical-products/{}/{}/code-list-scheme/{}/{}/codes-list - Getting code lists of code list scheme",
            groupAgencyId,
            groupId,
            logicalProductAgencyId,
            logicalProductId,
            codeListSchemeAgencyId,
            codeListSchemeId
        );
        try {
            List<PartialCodesList> codeLists =
                ddiService.getCodeListsByCodeListScheme(
                    codeListSchemeAgencyId,
                    codeListSchemeId
                );
            return ResponseEntity.ok(codeLists);
        } catch (Exception e) {
            logger.error(
                "Failed to get code lists for code list scheme: agencyId={}, codeListSchemeId={}",
                codeListSchemeAgencyId,
                codeListSchemeId,
                e
            );
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/groups/{agencyId}/{id}/codes-list")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<List<PartialCodesList>> getGroupCodesLists(
        @PathVariable String agencyId,
        @PathVariable(Constants.ID) String id
    ) {
        logger.info(
            "GET /ddi/groups/{}/{}/codes-list - Getting all code lists of group (all logical products / code list schemes)",
            agencyId,
            id
        );
        try {
            List<PartialCodesList> codeLists = ddiService.getCodeListsByGroup(
                agencyId,
                id
            );
            return ResponseEntity.ok(codeLists);
        } catch (Exception e) {
            logger.error(
                "Failed to get code lists for group: agencyId={}, id={}",
                agencyId,
                id,
                e
            );
            return ResponseEntity.internalServerError().build();
        }
    }

    private List<PartialGroup> resolveGroups() {
        return resolveByReadStampStrategy(
            ddiService::getGroupsFilteredByStamp,
            ddiService::getGroups
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
}
