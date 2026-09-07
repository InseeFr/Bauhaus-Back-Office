package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.Ddi4SchemaService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.Ddi4SchemaRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.Ddi4SchemaValidator;

import java.util.List;

public class DomainDdi4SchemaService implements Ddi4SchemaService {

    private final Ddi4SchemaRepository repository;
    private final Ddi4SchemaValidator validator;

    public DomainDdi4SchemaService(Ddi4SchemaRepository repository, Ddi4SchemaValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public String schemaDocument() {
        return repository.schemaDocument();
    }

    @Override
    public List<String> validate(String json) {
        return validator.validate(json);
    }
}
