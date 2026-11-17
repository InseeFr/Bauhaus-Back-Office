package fr.insee.rmes.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;

/** Assertions partagées par les tests unitaires des contrôleurs qui servent une liste en HAL+JSON. */
public final class HalJsonListAssertions {

    private HalJsonListAssertions() {}

    /** La réponse est un 200 HAL+JSON dont le corps est une liste de {@code expectedSize} éléments. */
    public static void assertHalJsonListOfSize(ResponseEntity<? extends Collection<?>> result, int expectedSize) {
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        assertNotNull(result.getBody());
        assertEquals(expectedSize, result.getBody().size());
    }
}
