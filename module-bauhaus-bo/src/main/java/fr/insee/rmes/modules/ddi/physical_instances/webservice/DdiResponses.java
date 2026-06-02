package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Helpers partagés par les contrôleurs DDI exposant un même item en XML (DDI 3.3) ou JSON (DDI 4) :
 * un résultat {@code null} (item introuvable) devient un {@code 404}, sinon un {@code 200} typé.
 */
final class DdiResponses {

    private DdiResponses() {
    }

    static ResponseEntity<String> xml(String xml) {
        if (xml == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xml);
    }

    static ResponseEntity<Ddi4Response> json(Ddi4Response ddi4) {
        if (ddi4 == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ddi4);
    }
}
