package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialLogicalProductResponse;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import java.util.List;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        value = "/ddi",
        produces = {"application/hal+json", MediaType.APPLICATION_JSON_VALUE})
public class LogicalProductResources {

    private final DDIService ddiService;

    public LogicalProductResources(DDIService ddiService) {
        this.ddiService = ddiService;
    }

    @GetMapping("/logical-product")
    @HasAccess(module = RBAC.Module.DDI_PHYSICALINSTANCE, privilege = RBAC.Privilege.READ)
    public ResponseEntity<List<PartialLogicalProductResponse>> getLogicalProducts() {
        List<PartialLogicalProduct> products = ddiService.getLogicalProducts();

        List<PartialLogicalProductResponse> responses = products.stream()
                .map(product -> {
                    var response = PartialLogicalProductResponse.fromDomain(product);
                    response.add(linkTo(LogicalProductResources.class)
                            .slash("logical-product")
                            .slash(product.agency())
                            .slash(product.id())
                            .withSelfRel());
                    return response;
                })
                .toList();

        return ResponseEntity.ok().contentType(MediaTypes.HAL_JSON).body(responses);
    }
}
