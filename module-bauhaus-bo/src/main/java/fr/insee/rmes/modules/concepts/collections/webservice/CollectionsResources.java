package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.commons.configuration.conditional.ConditionalOnModule;
import fr.insee.rmes.modules.concepts.collections.domain.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.PartialCollection;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/concepts/collections")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ConceptsCollections", description = "Concept Collections API")
@ConditionalOnModule("concepts")
public class CollectionsResources {
    private final CollectionService service;
    public CollectionsResources(CollectionService service) {
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
            return this.service.getCollection(id).map(
                    collection ->  ResponseEntity.ok().body(CollectionResponse.fromDomain(collection))
            ).orElse(ResponseEntity.notFound().build());
        } catch (CollectionsFetchException e) {
            return  ResponseEntity.internalServerError().build();
        }
    }
}
