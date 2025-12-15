package fr.insee.rmes.webservice;

import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.webservice.response.mixins.OperationFamilyMixin;
import org.springframework.boot.jackson2.autoconfigure.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebserviceConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addMixIns() {
        return builder -> builder.mixIn(
                OperationFamily.class,
                OperationFamilyMixin.class
        );
    }
}
