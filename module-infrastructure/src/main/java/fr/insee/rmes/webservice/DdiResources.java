package fr.insee.rmes.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.domain.model.ddi.Ddi3Response;
import fr.insee.rmes.domain.model.ddi.Ddi4Response;
import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.domain.port.clientside.DDIService;
import fr.insee.rmes.webservice.response.ddi.PartialPhysicalInstanceResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('ddi')")
public class DdiResources {

    private final DDIService ddiService;
    private final DDI4toDDI3ConverterService ddi4toDdi3ConverterService;
    private final DDI3toDDI4ConverterService ddi3toDdi4ConverterService;

    public DdiResources(DDIService ddiService,
                        DDI4toDDI3ConverterService ddi4toDdi3ConverterService,
                        DDI3toDDI4ConverterService ddi3toDdi4ConverterService) {
        this.ddiService = ddiService;
        this.ddi4toDdi3ConverterService = ddi4toDdi3ConverterService;
        this.ddi3toDdi4ConverterService = ddi3toDdi4ConverterService;
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
    public ResponseEntity<Ddi4Response> getDdi4PhysicalInstance(@PathVariable(Constants.ID) String id) {
        Ddi4Response response = ddiService.getDdi4PhysicalInstance(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PatchMapping("/physical-instance/{id}")
    public ResponseEntity<Ddi4Response> updatePhysicalInstance(
            @PathVariable String id,
            @RequestBody UpdatePhysicalInstanceRequest request) {
        Ddi4Response updatedInstance = ddiService.updatePhysicalInstance(id, request);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(updatedInstance);
    }

    @PostMapping("/convert/ddi4-to-ddi3")
    public ResponseEntity<Ddi3Response> convertDdi4ToDdi3(@RequestBody Ddi4Response ddi4) {
        Ddi3Response ddi3 = ddi4toDdi3ConverterService.convertDdi4ToDdi3(ddi4);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ddi3);
    }

    @PostMapping("/convert/ddi3-to-ddi4")
    public ResponseEntity<Ddi4Response> convertDdi3ToDdi4(@RequestBody Ddi3Response ddi3) {
        Ddi4Response ddi4 = ddi3toDdi4ConverterService.convertDdi3ToDdi4(ddi3);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ddi4);
    }
}