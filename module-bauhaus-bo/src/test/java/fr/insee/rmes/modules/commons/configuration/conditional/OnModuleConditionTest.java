package fr.insee.rmes.modules.commons.configuration.conditional;

import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.commons.configuration.OnModuleCondition;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OnModuleConditionTest {

    private static final String MODULES_PROPERTY = "fr.insee.rmes.bauhaus.modules";

    private final OnModuleCondition condition = new OnModuleCondition();

    @Test
    void shouldMatchWhenModuleIsDeclared() {
        ConditionOutcome outcome = evaluate("ddi", "concepts", "ddi", "operations");

        assertTrue(outcome.isMatch(), "Should match when module 'ddi' is declared in modules");
    }

    @Test
    void shouldNotMatchWhenModuleIsNotDeclared() {
        ConditionOutcome outcome = evaluate("ddi", "concepts", "operations");

        assertFalse(outcome.isMatch(), "Should not match when module 'ddi' is not declared in modules");
    }

    @Test
    void shouldNotMatchWhenModulesPropertyIsEmpty() {
        ConditionOutcome outcome = evaluate("ddi");

        assertFalse(outcome.isMatch(), "Should not match when modules property is empty");
    }

    @Test
    void shouldMatchWhenModuleIsTheOnlyDeclaredModule() {
        ConditionOutcome outcome = evaluate("ddi", "ddi");

        assertTrue(outcome.isMatch(), "Should match when module is the only declared module");
    }

    @Test
    void shouldNotMatchWhenAttributesAreNull() {
        ConditionContext context = mock(ConditionContext.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(null);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertFalse(outcome.isMatch(), "Should not match when annotation attributes are null");
    }

    private ConditionOutcome evaluate(String requiredModule, String... declaredModules) {
        MockEnvironment environment = new MockEnvironment();
        for (int i = 0; i < declaredModules.length; i++) {
            environment.setProperty(MODULES_PROPERTY + "[" + i + "].identifier", declaredModules[i]);
        }

        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName()))
                .thenReturn(Map.of("value", requiredModule));

        return condition.getMatchOutcome(context, metadata);
    }
}
