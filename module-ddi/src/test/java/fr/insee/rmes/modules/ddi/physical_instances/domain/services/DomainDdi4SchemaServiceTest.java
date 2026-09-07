package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.Ddi4SchemaRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.Ddi4SchemaValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DomainDdi4SchemaServiceTest {

    private final Ddi4SchemaRepository repository = mock(Ddi4SchemaRepository.class);
    private final Ddi4SchemaValidator validator = mock(Ddi4SchemaValidator.class);
    private final DomainDdi4SchemaService service = new DomainDdi4SchemaService(repository, validator);

    @Test
    void shouldServeTheSchemaDocument() {
        when(repository.schemaDocument()).thenReturn("{\"type\":\"object\"}");

        assertEquals("{\"type\":\"object\"}", service.schemaDocument());
    }

    @Test
    void shouldReportTheSchemaErrorsOfADocument() {
        when(validator.validate(any())).thenReturn(List.of("items: is missing"));

        assertEquals(List.of("items: is missing"), service.validate("{}"));
    }

    @Test
    void shouldReportNoErrorForAConformingDocument() {
        when(validator.validate(any())).thenReturn(List.of());

        assertEquals(List.of(), service.validate("{\"items\":[]}"));
    }
}
