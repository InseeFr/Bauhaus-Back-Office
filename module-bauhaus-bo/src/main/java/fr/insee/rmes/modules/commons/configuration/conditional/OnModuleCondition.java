package fr.insee.rmes.modules.commons.configuration.conditional;

import fr.insee.rmes.modules.clientconfig.domain.model.ModuleConfig;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.List;
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

        List<ModuleConfig> modules = Binder.get(context.getEnvironment())
                .bind(MODULES_PROPERTY, Bindable.listOf(ModuleConfig.class))
                .orElseGet(List::of);

        boolean declared = modules.stream().anyMatch(module -> requiredModule.equals(module.identifier()));
        if (declared) {
            return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnModule.class)
                    .foundExactly("module '" + requiredModule + "'"));
        }

        return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnModule.class)
                .because("module '" + requiredModule + "' not found in modules"));
    }
}
