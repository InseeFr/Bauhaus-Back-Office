package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.MalformedLocalisedLabelException;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/concepts/collections")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Collections V2", description = "Concept Collections API")
@ConditionalOnModule("concepts")
public class CollectionsResources {
    private final CollectionsService service;
    public CollectionsResources(CollectionsService service) {
        this.service = service;
    }

    @GetMapping
    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.READ)
    List<PartialCollectionResponse> getAll(){
        try {
            return this.service.getAllCollections().stream().map(PartialCollectionResponse::fromDomain).toList();
        } catch (CollectionsFetchException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.READ)
    CollectionResponse getById(@PathVariable String id){
        try {
            return this.service.getCollection(new CollectionId(id)).map(CollectionResponse::fromDomain).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection %s not found".formatted(id)));
        } catch (CollectionsFetchException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping
    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.CREATE)
    String create(@RequestBody CreateCollectionRequest collection, HttpServletResponse response){
        try {
            var collectionId = this.service.createCollection(collection.toCommand());

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(collectionId.value())
                    .toUri();

            response.setStatus(HttpStatus.CREATED.value());
            response.setHeader(HttpHeaders.LOCATION, location.toString());

            return collectionId.value();
        } catch (InvalidCreateCollectionCommandException | MalformedLocalisedLabelException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (CollectionsSaveException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.UPDATE)
    ResponseEntity<String> update(@PathVariable String id, @RequestBody String body){
        return null;
    }

    @DeleteMapping("/{id}")
    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.DELETE)
    ResponseEntity<String> delete(@PathVariable String id){
        return null;
    }

    @GetMapping("/search")
    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.READ)
    ResponseEntity<String> search(){
        return null;
    }

    @PutMapping("/{id}/validate")
    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.PUBLISH)
    ResponseEntity<String> publish(@PathVariable String id){
        return null;
    }
}
