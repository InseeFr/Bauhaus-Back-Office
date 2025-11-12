package fr.insee.rmes.modules.concepts.collection.webservice;

import fr.insee.rmes.modules.commons.configuration.conditional.ConditionalOnModule;
import fr.insee.rmes.modules.concepts.collection.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collection.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collection.domain.port.clientside.CollectionsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    ResponseEntity<List<PartialCollectionResponse>> getAllConceptsCollections(){
        try {
            var collections =  this.service.getAllCollections().stream().map(PartialCollectionResponse::fromDomain).toList();
            return ResponseEntity.ok().body(collections);
        } catch (CollectionsFetchException e) {
            return  ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    ResponseEntity<CollectionResponse> getConceptsCollectionById(@PathVariable String id){
        try {
            return this.service.getCollection(new CollectionId(id)).map(
                    collection ->  ResponseEntity.ok().body(CollectionResponse.fromDomain(collection))
            ).orElse(ResponseEntity.notFound().build());
        } catch (CollectionsFetchException e) {
            return  ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    ResponseEntity<String> createConcept( String id){
        return null;
    }
}
