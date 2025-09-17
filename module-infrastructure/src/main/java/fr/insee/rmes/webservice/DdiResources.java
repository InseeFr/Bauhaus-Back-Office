package fr.insee.rmes.webservice;

import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.PhysicalInstance;
import fr.insee.rmes.domain.port.clientside.DDIService;
import fr.insee.rmes.webservice.response.ddi.PartialPhysicalInstanceResponse;
import fr.insee.rmes.webservice.response.ddi.PhysicalInstanceResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(
        value = "/ddi",
        produces = {
                "application/hal+json",
                MediaType.APPLICATION_JSON_VALUE
        }
)
public class DdiResources {

    private final DDIService ddiService;

    public DdiResources(DDIService ddiService) {
        this.ddiService = ddiService;
    }

    @GetMapping("/physical-instance")
    public ResponseEntity<List<PartialPhysicalInstanceResponse>> getPhysicalInstances() {
        List<PartialPhysicalInstance> instances = ddiService.getPhysicalInstances();
        
        List<PartialPhysicalInstanceResponse> responses = instances.stream()
                .map(instance -> {
                    var response = PartialPhysicalInstanceResponse.fromDomain(instance);
                    response.add(linkTo(DdiResources.class).slash("physical-instance").slash(instance.id()).withSelfRel());
                    return response;
                })
                .toList();
        
        return ResponseEntity.ok()
                .contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
                .body(responses);
    }

    @GetMapping("/physical-instance/{id}")
    public PhysicalInstanceResponse getPhysicalInstanceById(@PathVariable String id) {
        PhysicalInstance instance = ddiService.getPhysicalInstance(id);
        var response = PhysicalInstanceResponse.fromDomain(instance);
        response.add(linkTo(DdiResources.class).slash("physical-instance").slash(id).withSelfRel());
        return response;
    }
}