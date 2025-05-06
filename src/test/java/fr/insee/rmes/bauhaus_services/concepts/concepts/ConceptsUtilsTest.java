package fr.insee.rmes.bauhaus_services.concepts.concepts;

import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.notes.NoteManager;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
class ConceptsUtilsTest {

    @InjectMocks
    ConceptsUtils conceptsUtils = new ConceptsUtils(new ConceptsPublication(),new NoteManager(),5);

    @MockitoBean
    StampsRestrictionsService stampsRestrictionsService;

    @Test
    void shouldThrowRmesUnauthorizedExceptionWhenSetConcept() throws RmesException {
        when(stampsRestrictionsService.canCreateConcept()).thenReturn(false);
        RmesException exception = assertThrows(RmesUnauthorizedException.class, () -> conceptsUtils.setConcept("Example"));
        assertTrue(exception.getDetails().contains("Only an admin or concepts manager can create a new concept."));
    }
}