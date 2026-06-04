package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.users.webservice.PublicEndpoint;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint #447 : {@code GET /structures/{agency}/{uuid}[/{version}]/variables} renvoie tous les
 * DataRelationship (et donc les variables) d'une PhysicalInstance, en DDI 3.3 XML (multi-fragments
 * {@code <FragmentInstance>}) ou DDI 4 JSON.
 *
 * <p>Le path est repris verbatim de l'issue : {@code {uuid}} désigne une PhysicalInstance. Ce
 * contrôleur dédié est mappé sur {@code /structures} sans toucher au {@code StructureResources}
 * du module structures (DSD) ; les patterns se terminent par le segment littéral {@code variables}.
 */
@RestController
@RequestMapping("/structures")
@ConditionalOnModule("ddi")
@PublicEndpoint
public class PhysicalInstanceVariablesResources {

    private final DDIService ddiService;

    public PhysicalInstanceVariablesResources(DDIService ddiService) {
        this.ddiService = ddiService;
    }

    @GetMapping(
        value = "/{agency}/{id}/{version}/variables",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<String> getVariablesXmlByVersion(
        @PathVariable String agency,
        @PathVariable String id,
        @PathVariable String version
    ) {
        return DdiResponses.xml(
            ddiService.getDataRelationshipsXml(agency, id, version)
        );
    }

    @GetMapping(
        value = "/{agency}/{id}/{version}/variables",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Ddi4Response> getVariablesJsonByVersion(
        @PathVariable String agency,
        @PathVariable String id,
        @PathVariable String version
    ) {
        return DdiResponses.json(
            ddiService.getDataRelationships(agency, id, version)
        );
    }

    @GetMapping(
        value = "/{agency}/{id}/variables",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<String> getVariablesXml(
        @PathVariable String agency,
        @PathVariable String id
    ) {
        return DdiResponses.xml(
            ddiService.getDataRelationshipsXml(agency, id, null)
        );
    }

    @GetMapping(
        value = "/{agency}/{id}/variables",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Ddi4Response> getVariablesJson(
        @PathVariable String agency,
        @PathVariable String id
    ) {
        return DdiResponses.json(
            ddiService.getDataRelationships(agency, id, null)
        );
    }
}
