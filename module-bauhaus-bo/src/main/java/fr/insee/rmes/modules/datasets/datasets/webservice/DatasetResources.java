package fr.insee.rmes.modules.datasets.datasets.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.datasets.datasets.model.Dataset;
import fr.insee.rmes.modules.datasets.datasets.model.DatasetsForSearch;
import fr.insee.rmes.modules.datasets.datasets.model.PartialDataset;
import fr.insee.rmes.modules.datasets.datasets.model.PatchDataset;
import fr.insee.rmes.modules.datasets.distributions.model.Distribution;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/datasets")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('datasets')")
public class DatasetResources {

    final DatasetService datasetService;

    public DatasetResources(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @GetMapping(produces = "application/json")
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.READ)
    public List<PartialDataset> getDatasets() throws RmesException {
        return this.datasetService.getDatasets();
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.READ)
    public Dataset getDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.datasetService.getDatasetByID(id);
    }

    @GetMapping("/{id}/distributions")
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.READ)
    public String getDistributionsByDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.datasetService.getDistributions(id);
    }

    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/search", produces = "application/json")
    public List<DatasetsForSearch> getDatasetsForSearch() throws RmesException {
        return this.datasetService.getDatasetsForSearch();
    }


    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.CREATE)
    public ResponseEntity<String> setDataset(@RequestBody String body) throws RmesException {
        String id = this.datasetService.create(body);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(id);
    }

    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public void setDataset(
            @PathVariable("id") String id,
            @RequestBody String body) throws RmesException {
        this.datasetService.update(id, body);
    }

    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.PUBLISH)
    @PutMapping(value = "/{id}/validate")
    public void publishDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        this.datasetService.publishDataset(id);
    }

    @GetMapping(value = "/archivageUnits", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.READ)
    public String getArchivageUnits() throws RmesException {
        return this.datasetService.getArchivageUnits();
    }

    @PatchMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.UPDATE)
    public void patchDataset(
            @PathVariable("id") String id,
            @RequestBody PatchDataset dataset
    ) throws RmesException {
        this.datasetService.patchDataset(id, dataset);
    }

    @DeleteMapping("/{id}")
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.DELETE)
    public ResponseEntity<Void> deleteDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        datasetService.deleteDatasetId(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}