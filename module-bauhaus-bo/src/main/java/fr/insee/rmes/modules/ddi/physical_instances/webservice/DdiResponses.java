package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * Helpers partagés par les contrôleurs DDI exposant un même item en XML (DDI 3.3) ou JSON (DDI 4) :
 * un résultat {@code null} (item introuvable) devient un {@code 404} au corps {@code ApiError}
 * (servi par le filet des erreurs), sinon un {@code 200} typé.
 */
final class DdiResponses {

    private DdiResponses() {}

    static ResponseEntity<String> xml(String xml) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(found(xml));
    }

    static <T> ResponseEntity<T> json(T ddi4) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(found(ddi4));
    }

    /** L'item lu, ou un {@code 404} s'il est introuvable. */
    static <T> T found(T item) {
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DDI item not found");
        }
        return item;
    }
}
