package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.commons.configuration.conditional.ConditionalOnModule;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/concepts/collections")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ConceptsCollections", description = "Concept Collections API")
@ConditionalOnModule("concepts")
public class CollectionsResources {
    private final CollectionsService service;
    public CollectionsResources(CollectionsService service) {
        this.service = service;
    }

    @GetMapping
    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.READ)
    ResponseEntity<List<PartialCollectionResponse>> getAll(){
        try {
            var collections =  this.service.getAllCollections().stream().map(PartialCollectionResponse::fromDomain).toList();
            return ResponseEntity.ok().body(collections);
        } catch (CollectionsFetchException e) {
            return  ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.READ)
    ResponseEntity<CollectionResponse> getById(@PathVariable String id){
        try {
            return this.service.getCollection(new CollectionId(id)).map(
                    collection ->  ResponseEntity.ok().body(CollectionResponse.fromDomain(collection))
            ).orElse(ResponseEntity.notFound().build());
        } catch (CollectionsFetchException e) {
            return  ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.CREATE)
    ResponseEntity<String> create(@RequestBody CreateCollectionRequest collection){
        try {
            var collectionId = this.service.createCollection(collection.toCommand());

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(collectionId.value())
                    .toUri();

            return ResponseEntity.created(location).body(collectionId.value());
        } catch (InvalidCreateCollectionCommandException | MalformedLocalisedLabelException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (CollectionsSaveException e) {
            return  ResponseEntity.internalServerError().build();
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
