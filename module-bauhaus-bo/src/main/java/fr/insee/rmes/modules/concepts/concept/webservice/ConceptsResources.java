package fr.insee.rmes.modules.concepts.concept.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptNotFoundException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptAlreadyPublishedException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsSaveException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidConceptIdException;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptForAdvancedSearch;
import fr.insee.rmes.model.concepts.PartialConcept;
import fr.insee.rmes.modules.concepts.concept.webservice.response.ConceptForAdvancedSearchResponse;
import fr.insee.rmes.modules.concepts.concept.webservice.response.ConceptToValidateResponse;
import fr.insee.rmes.modules.concepts.concept.webservice.response.PartialConceptResponse;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/concepts")
@ConditionalOnModule("concepts")
public class ConceptsResources {

    private final fr.insee.rmes.bauhaus_services.ConceptsService legacyConceptsService;
    private final fr.insee.rmes.modules.concepts.concept.domain.port.clientside.ConceptsService conceptsService;

    public ConceptsResources(
            fr.insee.rmes.bauhaus_services.ConceptsService legacyConceptsService,
            fr.insee.rmes.modules.concepts.concept.domain.port.clientside.ConceptsService conceptsService) {
        this.legacyConceptsService = legacyConceptsService;
        this.conceptsService = conceptsService;
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public ResponseEntity<List<PartialConceptResponse>> getConcepts() {
        try {
            List<PartialConceptResponse> responses = conceptsService.getAllConcepts().stream()
                    .map(compact -> new PartialConcept(compact.id().value(), compact.prefLabel().value(), null))
                    .map(partial -> {
                        var response = PartialConceptResponse.fromDomain(partial);
                        response.add(linkTo(ConceptsResources.class).slash("concept").slash(partial.id()).withSelfRel());
                        return response;
                    })
                    .toList();
            return ResponseEntity.ok()
                    .contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
                    .body(responses);
        } catch (ConceptsFetchException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.DELETE)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteConcept(@PathVariable(Constants.ID) String id) throws RmesException {
        try {
            conceptsService.deleteConcept(new ConceptId(id));
        } catch (ConceptNotFoundException e) {
            throw new RmesNotFoundException(e.getMessage());
        } catch (InvalidConceptIdException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (ConceptsFetchException | ConceptsSaveException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/advanced-search", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public ResponseEntity<List<ConceptForAdvancedSearchResponse>> getConceptsSearch() throws RmesException {
        List<ConceptForAdvancedSearch> concepts = legacyConceptsService.getConceptsSearch();

        List<ConceptForAdvancedSearchResponse> responses = concepts.stream()
                .map(concept -> {
                    var response = ConceptForAdvancedSearchResponse.fromDomain(concept);
                    response.add(linkTo(ConceptsResources.class).slash("concept").slash(concept.id()).withSelfRel());
                    return response;
                })
                .toList();

        return ResponseEntity.ok()
                .contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
                .body(responses);
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/concept/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getConceptByID(@PathVariable(Constants.ID) String id) throws RmesException {
        String concept = legacyConceptsService.getConceptByID(id);
        return ResponseEntity.status(HttpStatus.OK).body(concept);
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/toValidate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ConceptToValidateResponse>> getConceptsToValidate() {
        try {
            var responses = conceptsService.getConceptsToValidate().stream()
                    .map(ConceptToValidateResponse::fromDomain)
                    .toList();
            return ResponseEntity.ok(responses);
        } catch (ConceptsFetchException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/concept/{id}/links", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getConceptLinksByID(@PathVariable(Constants.ID) String id) throws RmesException {
        String conceptLinks = legacyConceptsService.getConceptLinksByID(id);
        return ResponseEntity.status(HttpStatus.OK).body(conceptLinks);
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/concept/{id}/notes/{conceptVersion}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getConceptNotesByID(@PathVariable(Constants.ID) String id, @PathVariable("conceptVersion") int conceptVersion) throws RmesException {
        String notes = legacyConceptsService.getConceptNotesByID(id, conceptVersion);
        return ResponseEntity.status(HttpStatus.OK).body(notes);
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.CREATE)
    @PostMapping(value = "/concept", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> setConcept(@RequestBody String body) throws RmesException {
        String id = legacyConceptsService.setConcept(body);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).body(id);
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/concept/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> setConcept(
            @PathVariable(Constants.ID) String id,
            @RequestBody String body) throws RmesException {
        legacyConceptsService.setConcept(id, body);
        return ResponseEntity.noContent().build();
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.PUBLISH)
    @PutMapping(value = "/{id}/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> setConceptsValidation(
            @PathVariable(Constants.ID) String id,
            @RequestBody List<String> idsToValidate) throws RmesException {
        try {
            List<ConceptId> conceptIds = idsToValidate.stream().map(ConceptId::new).toList();
            conceptsService.validateConcepts(conceptIds);
        } catch (InvalidConceptIdException | ConceptAlreadyPublishedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (ConceptsFetchException | ConceptsSaveException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return ResponseEntity.noContent().build();
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/concept/export/{id}", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/zip"})
    public ResponseEntity<?> exportConcept(@PathVariable(Constants.ID) String id, @RequestHeader(required = false) String accept) throws RmesException {
        return legacyConceptsService.exportConcept(id, accept);
    }

    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/concept/export-zip/{id}/{type}", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/zip"})
    public void exportZipConcept(
            @PathVariable(Constants.ID) String id,
            @PathVariable("type") String type,
            @RequestParam("langue") Language lg,
            @RequestHeader(required = false) String accept,
            @RequestParam("withConcepts") boolean withConcepts,
            HttpServletResponse response) throws RmesException {
        legacyConceptsService.exportZipConcept(id, accept, response, lg, type, withConcepts);
    }
}
