package fr.insee.rmes.modules.commons.webservice;

import fr.insee.rmes.modules.commons.domain.port.clientside.DisseminationStatusService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class DisseminationStatusResources {
    private final DisseminationStatusService disseminationStatusService;

    public DisseminationStatusResources(DisseminationStatusService disseminationStatusService) {
        this.disseminationStatusService = disseminationStatusService;
    }

    @GetMapping(value = "/disseminationStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DisseminationStatusDTO> getDisseminationStatus() {
        return this.disseminationStatusService.getDisseminationStatus().stream()
                .map(DisseminationStatusDTO::fromDomain)
                .toList();
    }
}
