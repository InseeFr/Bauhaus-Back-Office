package fr.insee.rmes.sparql.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyResolverTest {

    @Mock
    private Environment mockEnvironment;

    private PropertyResolver propertyResolver;

    @BeforeEach
    void setUp() throws Exception {
        this.setStaticEnvironment(null);

        this.propertyResolver = new PropertyResolver();
    }

    @Test
    void shouldReturnOriginalValueWhenEnvironmentIsNull() {
        final String input = "${test.property}";
        final String result = PropertyResolver.resolve(input);
        assertEquals(input, result);
    }

    @Test
    void shouldReturnOriginalValueWhenInputIsNull() throws Exception {
        this.setStaticEnvironment(this.mockEnvironment);
        
        final String result = PropertyResolver.resolve(null);
        assertNull(result);
    }

    @Test
    void shouldResolveSimpleProperty() throws Exception {
        this.setStaticEnvironment(this.mockEnvironment);
        when(this.mockEnvironment.getProperty("test.property", "")).thenReturn("resolved-value");
        
        final String input = "${test.property}";
        final String result = PropertyResolver.resolve(input);
        
        assertEquals("resolved-value", result);
        verify(this.mockEnvironment).getProperty("test.property", "");
    }

    @Test
    void shouldResolveMultipleProperties() throws Exception {
        this.setStaticEnvironment(this.mockEnvironment);
        when(this.mockEnvironment.getProperty("base.url", "")).thenReturn("http://example.com");
        when(this.mockEnvironment.getProperty("api.path", "")).thenReturn("/api/v1");
        
        final String input = "${base.url}${api.path}/endpoint";
        final String result = PropertyResolver.resolve(input);
        
        assertEquals("http://example.com/api/v1/endpoint", result);
        verify(this.mockEnvironment).getProperty("base.url", "");
        verify(this.mockEnvironment).getProperty("api.path", "");
    }

    @Test
    void shouldHandleNestedProperties() throws Exception {
        this.setStaticEnvironment(this.mockEnvironment);
        when(this.mockEnvironment.getProperty("outer.property", "")).thenReturn("${inner.property}");
        when(this.mockEnvironment.getProperty("inner.property", "")).thenReturn("final-value");
        
        final String input = "${outer.property}";
        final String result = PropertyResolver.resolve(input);
        
        assertEquals("final-value", result);
        verify(this.mockEnvironment).getProperty("outer.property", "");
        verify(this.mockEnvironment).getProperty("inner.property", "");
    }

    @Test
    void shouldReturnEmptyStringForUnknownProperty() throws Exception {
        this.setStaticEnvironment(this.mockEnvironment);
        when(this.mockEnvironment.getProperty("unknown.property", "")).thenReturn("");
        
        final String input = "${unknown.property}";
        final String result = PropertyResolver.resolve(input);
        
        assertEquals("", result);
        verify(this.mockEnvironment).getProperty("unknown.property", "");
    }

    @Test
    void shouldHandleMalformedProperty() throws Exception {
        this.setStaticEnvironment(this.mockEnvironment);
        
        final String input = "${malformed.property";
        final String result = PropertyResolver.resolve(input);
        
        assertEquals(input, result);
        verifyNoInteractions(this.mockEnvironment);
    }

    @Test
    void shouldHandleEmptyPropertyName() throws Exception {
        this.setStaticEnvironment(this.mockEnvironment);
        when(this.mockEnvironment.getProperty("", "")).thenReturn("");
        
        final String input = "${}";
        final String result = PropertyResolver.resolve(input);
        
        assertEquals("", result);
        verify(this.mockEnvironment).getProperty("", "");
    }

    @Test
    void shouldHandlePropertyWithoutPlaceholder() throws Exception {
        this.setStaticEnvironment(this.mockEnvironment);
        
        final String input = "plain-text-without-placeholder";
        final String result = PropertyResolver.resolve(input);
        
        assertEquals(input, result);
        verifyNoInteractions(this.mockEnvironment);
    }

    @Test
    void shouldResolveComplexConcatenation() throws Exception {
        this.setStaticEnvironment(this.mockEnvironment);
        when(this.mockEnvironment.getProperty("fr.insee.rmes.bauhaus.baseGraph", "")).thenReturn("http://rdf.insee.fr/graphes/");
        when(this.mockEnvironment.getProperty("fr.insee.rmes.bauhaus.codelists.graph", "")).thenReturn("codes-listes");
        
        final String input = "${fr.insee.rmes.bauhaus.baseGraph}${fr.insee.rmes.bauhaus.codelists.graph}";
        final String result = PropertyResolver.resolve(input);
        
        assertEquals("http://rdf.insee.fr/graphes/codes-listes", result);
        verify(this.mockEnvironment).getProperty("fr.insee.rmes.bauhaus.baseGraph", "");
        verify(this.mockEnvironment).getProperty("fr.insee.rmes.bauhaus.codelists.graph", "");
    }

    @Test
    void shouldHandlePartialResolution() throws Exception {
        this.setStaticEnvironment(this.mockEnvironment);
        when(this.mockEnvironment.getProperty("known.property", "")).thenReturn("known-value");
        when(this.mockEnvironment.getProperty("unknown.property", "")).thenReturn("");
        
        final String input = "${known.property}-${unknown.property}-suffix";
        final String result = PropertyResolver.resolve(input);
        
        assertEquals("known-value--suffix", result);
        verify(this.mockEnvironment).getProperty("known.property", "");
        verify(this.mockEnvironment).getProperty("unknown.property", "");
    }

    @Test
    void shouldSetEnvironmentThroughSetter() throws Exception {
        this.propertyResolver.setEnvironment(this.mockEnvironment);
        
        final Environment staticEnv = this.getStaticEnvironment();
        assertSame(this.mockEnvironment, staticEnv);
    }


    private void setStaticEnvironment(final Environment env) throws Exception {
        final Field field = PropertyResolver.class.getDeclaredField("environment");
        field.setAccessible(true);
        field.set(null, env);
    }

    private Environment getStaticEnvironment() throws Exception {
        final Field field = PropertyResolver.class.getDeclaredField("environment");
        field.setAccessible(true);
        return (Environment) field.get(null);
    }
}