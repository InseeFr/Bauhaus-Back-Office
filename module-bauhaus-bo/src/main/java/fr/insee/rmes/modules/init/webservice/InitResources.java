package fr.insee.rmes.modules.init.webservice;

import fr.insee.rmes.modules.init.domain.port.clientside.InitService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class InitResources {

    private final InitService service;

    public InitResources(InitService service) {
        this.service = service;
    }

    @GetMapping(value = "/init", produces = MediaType.APPLICATION_JSON_VALUE)
    public InitResponse getProperties() {
        return InitResponse.fromDomain(service.getInitProperties());
    }
}
