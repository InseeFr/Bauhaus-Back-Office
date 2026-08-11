package fr.insee.rmes.modules.operations.families;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import fr.insee.rmes.modules.operations.families.webservice.response.OperationFamilyMixin;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FamiliesJacksonConfiguration {

    private final ObjectMapper objectMapper;

    public FamiliesJacksonConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void addMixIns() {
        objectMapper.addMixIn(OperationFamily.class, OperationFamilyMixin.class);
    }
}
