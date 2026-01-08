package fr.insee.rmes.modules.commons.configuration.conditional;

import fr.insee.rmes.modules.commons.configuration.conditional.ConditionalOnModule;
import fr.insee.rmes.modules.commons.configuration.conditional.OnModuleCondition;
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
        // Given
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "ddi");

        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("fr.insee.rmes.bauhaus.activeModules", "")).thenReturn("concepts,ddi,operations");
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(attributes);

        // When
        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        // Then
        assertTrue(outcome.isMatch(), "Should match when module 'ddi' is in active modules");
    }

    @Test
    void shouldNotMatchWhenModuleIsNotActive() {
        // Given
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "ddi");

        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("fr.insee.rmes.bauhaus.activeModules", "")).thenReturn("concepts,operations");
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(attributes);

        // When
        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        // Then
        assertFalse(outcome.isMatch(), "Should not match when module 'ddi' is not in active modules");
    }

    @Test
    void shouldNotMatchWhenModulesPropertyIsEmpty() {
        // Given
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "ddi");

        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("fr.insee.rmes.bauhaus.activeModules", "")).thenReturn("");
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(attributes);

        // When
        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        // Then
        assertFalse(outcome.isMatch(), "Should not match when activeModules property is empty");
    }

    @Test
    void shouldNotMatchWhenAttributesAreNull() {
        // Given
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        when(context.getEnvironment()).thenReturn(environment);
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(null);

        // When
        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        // Then
        assertFalse(outcome.isMatch(), "Should not match when annotation attributes are null");
    }

    @Test
    void shouldMatchWhenModuleIsOnlyActiveModule() {
        // Given
        OnModuleCondition condition = new OnModuleCondition();
        ConditionContext context = mock(ConditionContext.class);
        Environment environment = mock(Environment.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "ddi");

        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("fr.insee.rmes.bauhaus.activeModules", "")).thenReturn("ddi");
        when(metadata.getAnnotationAttributes(ConditionalOnModule.class.getName())).thenReturn(attributes);

        // When
        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        // Then
        assertTrue(outcome.isMatch(), "Should match when module is the only active module");
    }
}