package fr.insee.rmes.modules.commons.configuration.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * Condition that checks if a specific module is active based on the
 * {@code fr.insee.rmes.bauhaus.activeModules} property.
 */
public class OnModuleCondition extends SpringBootCondition {

    private static final String ACTIVE_MODULES_PROPERTY = "fr.insee.rmes.bauhaus.activeModules";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnModule.class.getName());

        if (attributes == null || !attributes.containsKey("value")) {
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnModule.class)
                    .because("no module specified"));
        }

        String requiredModule = (String) attributes.get("value");
        String activeModules = context.getEnvironment().getProperty(ACTIVE_MODULES_PROPERTY, "");

        if (activeModules.contains(requiredModule)) {
            return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnModule.class)
                    .foundExactly("module '" + requiredModule + "' in active modules: " + activeModules));
        } else {
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnModule.class)
                    .because("module '" + requiredModule + "' not found in active modules: " + activeModules));
        }
    }
}