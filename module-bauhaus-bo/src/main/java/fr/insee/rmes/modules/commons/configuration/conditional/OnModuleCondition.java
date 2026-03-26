package fr.insee.rmes.modules.commons.configuration.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

public class OnModuleCondition extends SpringBootCondition {

    private static final String MODULES_PROPERTY = "fr.insee.rmes.bauhaus.modules";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnModule.class.getName());

        if (attributes == null || !attributes.containsKey("value")) {
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnModule.class)
                    .because("no module specified"));
        }

        String requiredModule = (String) attributes.get("value");
        Environment environment = context.getEnvironment();

        int i = 0;
        while (true) {
            String identifier = environment.getProperty(MODULES_PROPERTY + "[" + i + "].identifier");
            if (identifier == null) {
                break;
            }
            if (identifier.equals(requiredModule)) {
                boolean disabled = Boolean.parseBoolean(environment.getProperty(MODULES_PROPERTY + "[" + i + "].disabled", "false"));
                if (!disabled) {
                    return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnModule.class)
                            .foundExactly("module '" + requiredModule + "' found and enabled"));
                } else {
                    return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnModule.class)
                            .because("module '" + requiredModule + "' is disabled"));
                }
            }
            i++;
        }

        return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnModule.class)
                .because("module '" + requiredModule + "' not found in modules"));
    }
}