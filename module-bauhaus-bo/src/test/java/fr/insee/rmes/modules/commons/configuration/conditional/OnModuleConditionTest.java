package fr.insee.rmes.modules.commons.configuration.conditional;

import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.commons.configuration.OnModuleCondition;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OnModuleConditionTest {

    @Test
    void shouldMatchWhenModuleIsActive() {
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "ddi");

        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[0].identifier")).thenReturn("concepts");
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[1].identifier")).thenReturn("ddi");
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[1].disabled", "false")).thenReturn("false");
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[2].identifier")).thenReturn("operations");
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertTrue(outcome.isMatch(), "Should match when module 'ddi' is in modules and not disabled");
    }

    @Test
    void shouldNotMatchWhenModuleIsNotActive() {
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "ddi");

        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[0].identifier")).thenReturn("concepts");
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[1].identifier")).thenReturn("operations");
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[2].identifier")).thenReturn(null);
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertFalse(outcome.isMatch(), "Should not match when module 'ddi' is not in modules");
    }

    @Test
    void shouldNotMatchWhenModulesPropertyIsEmpty() {
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "ddi");

        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[0].identifier")).thenReturn(null);
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertFalse(outcome.isMatch(), "Should not match when modules property is empty");
    }

    @Test
    void shouldNotMatchWhenAttributesAreNull() {
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        when(context.getEnvironment()).thenReturn(environment);
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(null);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertFalse(outcome.isMatch(), "Should not match when annotation attributes are null");
    }

    @Test
    void shouldMatchWhenModuleIsOnlyActiveModule() {
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "ddi");

        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[0].identifier")).thenReturn("ddi");
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[0].disabled", "false")).thenReturn("false");
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[1].identifier")).thenReturn(null);
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertTrue(outcome.isMatch(), "Should match when module is the only active module");
    }

    @Test
    void shouldNotMatchWhenModuleIsDisabled() {
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "ddi");

        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[0].identifier")).thenReturn("ddi");
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[0].disabled", "false")).thenReturn("true");
        when(environment.getProperty("fr.insee.rmes.bauhaus.modules[1].identifier")).thenReturn(null);
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertFalse(outcome.isMatch(), "Should not match when module 'ddi' is disabled");
    }
}
