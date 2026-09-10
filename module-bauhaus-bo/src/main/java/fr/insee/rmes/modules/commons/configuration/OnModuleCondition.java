package fr.insee.rmes.modules.commons.configuration;

import fr.insee.rmes.modules.clientconfig.domain.model.ModuleSettings;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnModuleCondition extends SpringBootCondition {

    private static final String MODULES_PROPERTY = "fr.insee.rmes.bauhaus.modules";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnModule.class.getName());

        if (attributes == null || !attributes.containsKey("value")) {
            return ConditionOutcome.noMatch(
                    ConditionMessage.forCondition(ConditionalOnModule.class).because("no module specified"));
        }

        String requiredModule = (String) attributes.get("value");

        Map<String, ModuleSettings> modules = Binder.get(context.getEnvironment())
                .bind(MODULES_PROPERTY, Bindable.mapOf(String.class, ModuleSettings.class))
                .orElseGet(Map::of);

        ModuleSettings settings = modules.get(requiredModule);
        if (settings != null && settings.enabled()) {
            return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnModule.class)
                    .foundExactly("module '" + requiredModule + "'"));
        }

        return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnModule.class)
                .because("module '" + requiredModule + "' is not an enabled module"));
    }
}
