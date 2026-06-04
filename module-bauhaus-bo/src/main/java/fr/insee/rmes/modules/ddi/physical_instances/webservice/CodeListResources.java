package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.commons.security.PublicEndpoint;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint #485 : {@code GET /codelist/{agency}/{uuid}[/{version}]} renvoie une CodeList et ses
 * Categories référencées, en DDI 3.3 XML (multi-fragments {@code <FragmentInstance>}) ou DDI 4 JSON,
 * sur le modèle de {@code /operation/{id}/studyUnit}.
 */
@RestController
@RequestMapping("/codelist")
@ConditionalOnModule("ddi")
@PublicEndpoint
public class CodeListResources {

    private final DDIService ddiService;

    public CodeListResources(DDIService ddiService) {
        this.ddiService = ddiService;
    }

    @GetMapping(
        value = "/{agency}/{id}/{version}",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<String> getCodeListXmlByVersion(
        @PathVariable String agency,
        @PathVariable String id,
        @PathVariable String version
    ) {
        return DdiResponses.xml(ddiService.getCodeListXml(agency, id, version));
    }

    @GetMapping(
        value = "/{agency}/{id}/{version}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Ddi4Response> getCodeListJsonByVersion(
        @PathVariable String agency,
        @PathVariable String id,
        @PathVariable String version
    ) {
        return DdiResponses.json(ddiService.getCodeList(agency, id, version));
    }

    @GetMapping(
        value = "/{agency}/{id}",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<String> getCodeListXml(
        @PathVariable String agency,
        @PathVariable String id
    ) {
        return DdiResponses.xml(ddiService.getCodeListXml(agency, id, null));
    }

    @GetMapping(
        value = "/{agency}/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Ddi4Response> getCodeListJson(
        @PathVariable String agency,
        @PathVariable String id
    ) {
        return DdiResponses.json(ddiService.getCodeList(agency, id, null));
    }
}
