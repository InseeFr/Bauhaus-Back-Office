package fr.insee.rmes.modules.operations.families.webservice;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import fr.insee.rmes.Constants;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyAlreadyPublishedException;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyNotFoundException;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyPrefLabelAlreadyUsedException;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;
import fr.insee.rmes.modules.operations.families.domain.port.clientside.FamilyService;
import fr.insee.rmes.modules.operations.families.webservice.response.OperationFamilyResponse;
import fr.insee.rmes.modules.operations.families.webservice.response.OperationFamilySeriesResponse;
import fr.insee.rmes.modules.operations.families.webservice.response.OperationFamilySeriesWithReportResponse;
import fr.insee.rmes.modules.operations.families.webservice.response.OperationFamilySubjectResponse;
import fr.insee.rmes.modules.operations.families.webservice.response.PartialOperationFamilyResponse;
import fr.insee.rmes.modules.operations.series.webservice.SeriesResources;
import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Qualifier("Family")
@RestController
@RequestMapping(
        value = "/operations",
        produces = {"application/hal+json", MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
@ConditionalOnModule("operations")
public class FamilyResources {

    protected final FamilyService familyService;

    public FamilyResources(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/families")
    @HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege = RBAC.Privilege.READ)
    public ResponseEntity<List<PartialOperationFamilyResponse>> getFamilies() throws RmesException {
        List<PartialOperationFamily> families = familyService.getFamilies();

        List<PartialOperationFamilyResponse> responses = families.stream()
                .map(family -> {
                    var response = PartialOperationFamilyResponse.fromDomain(family);
                    response.add(linkTo(FamilyResources.class)
                            .slash("family")
                            .slash(family.id())
                            .withSelfRel());
                    return response;
                })
                .toList();

        return ResponseEntity.ok()
                .contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
                .body(responses);
    }

    @GetMapping("/family/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege = RBAC.Privilege.READ)
    public OperationFamilyResponse getFamilyByID(@PathVariable(Constants.ID) String id) throws RmesException {
        var family = familyService.getFamily(id);
        return OperationFamilyResponse.fromDomain(
                family,
                family.series().stream()
                        .map(series -> {
                            var response = OperationFamilySeriesResponse.fromDomain(series);
                            response.add(linkTo(SeriesResources.class)
                                    .slash("series")
                                    .slash(series.id())
                                    .withSelfRel());
                            return response;
                        })
                        .toList(),
                family.subjects().stream()
                        .map(OperationFamilySubjectResponse::fromDomain)
                        .toList());
    }

    @GetMapping("/families/{id}/seriesWithReport")
    @HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege = RBAC.Privilege.READ)
    public ResponseEntity<List<OperationFamilySeriesWithReportResponse>> getSeriesWithReport(
            @PathVariable(Constants.ID) String id) throws RmesException {
        List<OperationFamilySeriesWithReportResponse> series = familyService.getSeriesWithReport(id).stream()
                .map(OperationFamilySeriesWithReportResponse::fromDomain)
                .toList();
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(series);
    }

    @PutMapping("/family/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege = RBAC.Privilege.UPDATE)
    public ResponseEntity<Object> setFamilyById(
            @PathVariable(Constants.ID) String id, @Valid @RequestBody FamilyRequest family) throws RmesException {
        try {
            familyService.updateFamily(family.toUpdateCommand(id));
        } catch (FamilyNotFoundException e) {
            throw new RmesNotFoundException(
                    ErrorCodes.FAMILY_UNKNOWN_ID, e.getMessage(), "Can't update non-existant family");
        } catch (FamilyPrefLabelAlreadyUsedException e) {
            throw prefLabelAlreadyUsed(e);
        }
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @PostMapping("/family")
    @HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege = RBAC.Privilege.CREATE)
    public ResponseEntity<Object> createFamily(@Valid @RequestBody FamilyRequest family) throws RmesException {
        String id;
        try {
            id = familyService.createFamily(family.toCreateCommand());
        } catch (FamilyPrefLabelAlreadyUsedException e) {
            throw prefLabelAlreadyUsed(e);
        }
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege = RBAC.Privilege.PUBLISH)
    @PutMapping("/family/{id}/validate")
    public ResponseEntity<Object> setFamilyValidation(@PathVariable(Constants.ID) String id) throws RmesException {
        try {
            familyService.validateFamily(id);
        } catch (FamilyAlreadyPublishedException _) {
            throw new RmesBadRequestException(
                    ErrorCodes.ALREADY_PUBLISHED, "This family is already published", "Family: " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    /**
     * Le front retrouve le libellé traduit du message à partir du code d'erreur : celui-ci fait
     * partie du contrat, il ne peut pas être remplacé par un texte libre.
     */
    private static RmesBadRequestException prefLabelAlreadyUsed(FamilyPrefLabelAlreadyUsedException exception) {
        return new RmesBadRequestException(
                exception.language() == Language.lg1
                        ? ErrorCodes.OPERATION_FAMILY_EXISTING_PREF_LABEL_LG1
                        : ErrorCodes.OPERATION_FAMILY_EXISTING_PREF_LABEL_LG2,
                exception.getMessage());
    }
}
