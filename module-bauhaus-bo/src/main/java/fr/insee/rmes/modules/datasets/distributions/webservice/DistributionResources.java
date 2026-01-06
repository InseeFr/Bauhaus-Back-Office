package fr.insee.rmes.modules.datasets.distributions.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.datasets.datasets.model.PartialDataset;
import fr.insee.rmes.modules.datasets.distributions.model.Distribution;
import fr.insee.rmes.modules.datasets.distributions.model.DistributionsForSearch;
import fr.insee.rmes.modules.datasets.distributions.model.PatchDistribution;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/distribution")
@ConditionalOnModule("datasets")
public class DistributionResources {

    final DistributionService distributionService;
    final DatasetService datasetService;

    final UserDecoder userDecoder;

    public DistributionResources(DistributionService distributionService, DatasetService datasetService, UserDecoder userDecoder) {
        this.distributionService = distributionService;
        this.datasetService = datasetService;
        this.userDecoder = userDecoder;
    }

    @GetMapping
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    public ResponseEntity<List<PartialDistributionResponse>> getDistributions() throws RmesException {
        List<PartialDistributionResponse> responses = this.distributionService.getDistributions().stream()
                .map(distribution -> {
                    var response = PartialDistributionResponse.fromDomain(distribution);
                    response.add(linkTo(DistributionResources.class).slash(distribution.id()).withSelfRel());
                    return response;
                })
                .toList();

        return ResponseEntity.ok()
                .contentType(MediaTypes.HAL_JSON)
                .body(responses);
    }

    @GetMapping("/{id}")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    public Distribution getDistribution(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.distributionService.getDistributionByID(id);
    }

    @PutMapping(value = "/{id}/validate")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.PUBLISH)
    public void publishDistribution(@PathVariable(Constants.ID) String id) throws RmesException {
        this.distributionService.publishDistribution(id);
    }

    @GetMapping("/datasets")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    public List<PartialDataset> getDatasetsForDistributionCreation(@AuthenticationPrincipal Object principal) throws RmesException, MissingUserInformationException {
        var user = userDecoder.fromPrincipal(principal).get();

        if (user.hasRole(Roles.ADMIN)) {
            return this.datasetService.getDatasets();
        }
        return this.datasetService.getDatasetsForDistributionCreation(user.getStamps());
    }

    @GetMapping(value = "/search", produces = "application/json")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    public List<DistributionsForSearch> getDistributionsForSearch() throws RmesException {
        return this.distributionService.getDistributionsForSearch();
    }


    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.CREATE)
    @ResponseStatus(HttpStatus.CREATED)
    public String createDistribution(
            @RequestBody String body) throws RmesException {
        return this.distributionService.create(body);
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.UPDATE)
    public String updateDistribution(
            @PathVariable("id") String id,
            @RequestBody String body) throws RmesException {
        return this.distributionService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.DELETE)
    public ResponseEntity<Void> deleteDistribution(
            @PathVariable(Constants.ID) String id) throws RmesException{
        distributionService.deleteDistributionId(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.UPDATE)
    public void patchDistribution(
            @PathVariable("id") String id,
            @RequestBody PatchDistribution distribution
    ) throws RmesException{
        this.distributionService.patchDistribution(id, distribution);
    }
}