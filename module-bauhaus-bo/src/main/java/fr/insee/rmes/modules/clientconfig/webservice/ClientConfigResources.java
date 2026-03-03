package fr.insee.rmes.modules.clientconfig.webservice;

import fr.insee.rmes.modules.clientconfig.domain.port.clientside.ClientConfigService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ClientConfigResources {

    private final ClientConfigService service;

    public ClientConfigResources(ClientConfigService service) {
        this.service = service;
    }

    @GetMapping(value = "/init", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClientConfigResponse getProperties() {
        return ClientConfigResponse.fromDomain(service.getClientConfigProperties());
    }
}
