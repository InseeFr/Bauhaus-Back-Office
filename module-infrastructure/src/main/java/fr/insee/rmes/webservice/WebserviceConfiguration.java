package fr.insee.rmes.webservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.webservice.response.mixins.OperationFamilyMixin;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebserviceConfiguration {

    private final ObjectMapper objectMapper;

    public WebserviceConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void addMixIns() {
        objectMapper.addMixIn(OperationFamily.class, OperationFamilyMixin.class);
    }
}
