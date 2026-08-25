package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.MissingValuesRepresentationInUseException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.MissingValuesRepresentationNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CategoryCodeListUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for CodeList usage queries.
 */
@RestController
@RequestMapping(value = "/ddi", produces = MediaType.APPLICATION_JSON_VALUE)
public class CodesListResources {

    private static final Logger logger = LoggerFactory.getLogger(
        CodesListResources.class
    );

    private final DDIService ddiService;

    public CodesListResources(DDIService ddiService) {
        this.ddiService = ddiService;
    }

    /**
     * Returns every Variable (with its PhysicalInstance) that uses the code list {@code agencyId/id}.
     */
    @GetMapping("/codes-list/{agencyId}/{id}/users")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<List<CodeListVariableUsage>> getCodeListUsers(
        @PathVariable String agencyId,
        @PathVariable String id
    ) {
        logger.info(
            "GET /ddi/codes-list/{}/{}/users - Getting variables using code list",
            agencyId,
            id
        );
        try {
            List<CodeListVariableUsage> usages =
                ddiService.getVariablesUsingCodeList(agencyId, id);
            return ResponseEntity.ok(usages);
        } catch (Exception e) {
            logger.error(
                "Failed to get variables using code list: agencyId={}, id={}",
                agencyId,
                id,
                e
            );
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Les listes de codes dont au moins un code référence la catégorie {@code agencyId/id}.
     * Alimente la popup de confirmation « catégorie partagée » côté front.
     */
    @GetMapping("/category/{agencyId}/{id}/users")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<List<CategoryCodeListUsage>> getCategoryUsers(
        @PathVariable String agencyId,
        @PathVariable String id
    ) {
        logger.info(
            "GET /ddi/category/{}/{}/users - Getting code lists using category",
            agencyId,
            id
        );
        try {
            List<CategoryCodeListUsage> usages =
                ddiService.getCodeListsUsingCategory(agencyId, id);
            return ResponseEntity.ok(usages);
        } catch (Exception e) {
            logger.error(
                "Failed to get code lists using category: agencyId={}, id={}",
                agencyId,
                id,
                e
            );
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Les variables (avec leur PhysicalInstance et StudyUnit) qui référencent la
     * ManagedMissingValuesRepresentation {@code agencyId/id} (valeurs sentinelles, cf. #1566).
     * Alimente la règle lecture seule/écriture de la section sentinelles.
     */
    @GetMapping("/missing-values-representations/{agencyId}/{id}/users")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.READ
    )
    public ResponseEntity<List<CodeListVariableUsage>> getMissingValuesRepresentationUsers(
        @PathVariable String agencyId,
        @PathVariable String id
    ) {
        logger.info(
            "GET /ddi/missing-values-representations/{}/{}/users - Getting variables using MMVR",
            agencyId,
            id
        );
        try {
            List<CodeListVariableUsage> usages =
                ddiService.getVariablesUsingMissingValuesRepresentation(agencyId, id);
            return ResponseEntity.ok(usages);
        } catch (Exception e) {
            logger.error(
                "Failed to get variables using missing values representation: agencyId={}, id={}",
                agencyId,
                id,
                e
            );
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprime une ManagedMissingValuesRepresentation sans usage (valeurs sentinelles, cf. #1566) :
     * défilage des schemes du groupe puis suppression de la MMVR, de sa CodeList de sentinelles et
     * des catégories de celle-ci. 409 si au moins une variable la référence encore.
     */
    @DeleteMapping("/missing-values-representations/{agencyId}/{id}")
    @HasAccess(
        module = RBAC.Module.DDI_PHYSICALINSTANCE,
        privilege = RBAC.Privilege.UPDATE
    )
    public ResponseEntity<Void> deleteMissingValuesRepresentation(
        @PathVariable String agencyId,
        @PathVariable String id
    ) {
        logger.info(
            "DELETE /ddi/missing-values-representations/{}/{} - Deleting orphan MMVR",
            agencyId,
            id
        );
        try {
            ddiService.deleteMissingValuesRepresentation(agencyId, id);
            return ResponseEntity.noContent().build();
        } catch (MissingValuesRepresentationNotFoundException e) {
            logger.warn("Missing values representation {}/{} not found: {}",
                agencyId, id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (MissingValuesRepresentationInUseException e) {
            logger.warn("Refused to delete missing values representation {}/{}: {}",
                agencyId, id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error(
                "Failed to delete missing values representation: agencyId={}, id={}",
                agencyId,
                id,
                e
            );
            return ResponseEntity.internalServerError().build();
        }
    }
}
