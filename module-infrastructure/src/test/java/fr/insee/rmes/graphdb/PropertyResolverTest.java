package fr.insee.rmes.graphdb;

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
        setStaticEnvironment(null);
        
        propertyResolver = new PropertyResolver();
    }

    @Test
    void shouldReturnOriginalValueWhenEnvironmentIsNull() {
        String input = "${test.property}";
        String result = PropertyResolver.resolve(input);
        assertEquals(input, result);
    }

    @Test
    void shouldReturnOriginalValueWhenInputIsNull() throws Exception {
        setStaticEnvironment(mockEnvironment);
        
        String result = PropertyResolver.resolve(null);
        assertNull(result);
    }

    @Test
    void shouldResolveSimpleProperty() throws Exception {
        setStaticEnvironment(mockEnvironment);
        when(mockEnvironment.getProperty("test.property", "")).thenReturn("resolved-value");
        
        String input = "${test.property}";
        String result = PropertyResolver.resolve(input);
        
        assertEquals("resolved-value", result);
        verify(mockEnvironment).getProperty("test.property", "");
    }

    @Test
    void shouldResolveMultipleProperties() throws Exception {
        setStaticEnvironment(mockEnvironment);
        when(mockEnvironment.getProperty("base.url", "")).thenReturn("http://example.com");
        when(mockEnvironment.getProperty("api.path", "")).thenReturn("/api/v1");
        
        String input = "${base.url}${api.path}/endpoint";
        String result = PropertyResolver.resolve(input);
        
        assertEquals("http://example.com/api/v1/endpoint", result);
        verify(mockEnvironment).getProperty("base.url", "");
        verify(mockEnvironment).getProperty("api.path", "");
    }

    @Test
    void shouldHandleNestedProperties() throws Exception {
        setStaticEnvironment(mockEnvironment);
        when(mockEnvironment.getProperty("outer.property", "")).thenReturn("${inner.property}");
        when(mockEnvironment.getProperty("inner.property", "")).thenReturn("final-value");
        
        String input = "${outer.property}";
        String result = PropertyResolver.resolve(input);
        
        assertEquals("final-value", result);
        verify(mockEnvironment).getProperty("outer.property", "");
        verify(mockEnvironment).getProperty("inner.property", "");
    }

    @Test
    void shouldReturnEmptyStringForUnknownProperty() throws Exception {
        setStaticEnvironment(mockEnvironment);
        when(mockEnvironment.getProperty("unknown.property", "")).thenReturn("");
        
        String input = "${unknown.property}";
        String result = PropertyResolver.resolve(input);
        
        assertEquals("", result);
        verify(mockEnvironment).getProperty("unknown.property", "");
    }

    @Test
    void shouldHandleMalformedProperty() throws Exception {
        setStaticEnvironment(mockEnvironment);
        
        String input = "${malformed.property";
        String result = PropertyResolver.resolve(input);
        
        assertEquals(input, result);
        verifyNoInteractions(mockEnvironment);
    }

    @Test
    void shouldHandleEmptyPropertyName() throws Exception {
        setStaticEnvironment(mockEnvironment);
        when(mockEnvironment.getProperty("", "")).thenReturn("");
        
        String input = "${}";
        String result = PropertyResolver.resolve(input);
        
        assertEquals("", result);
        verify(mockEnvironment).getProperty("", "");
    }

    @Test
    void shouldHandlePropertyWithoutPlaceholder() throws Exception {
        setStaticEnvironment(mockEnvironment);
        
        String input = "plain-text-without-placeholder";
        String result = PropertyResolver.resolve(input);
        
        assertEquals(input, result);
        verifyNoInteractions(mockEnvironment);
    }

    @Test
    void shouldResolveComplexConcatenation() throws Exception {
        setStaticEnvironment(mockEnvironment);
        when(mockEnvironment.getProperty("fr.insee.rmes.bauhaus.baseGraph", "")).thenReturn("http://rdf.insee.fr/graphes/");
        when(mockEnvironment.getProperty("fr.insee.rmes.bauhaus.codelists.graph", "")).thenReturn("codes-listes");
        
        String input = "${fr.insee.rmes.bauhaus.baseGraph}${fr.insee.rmes.bauhaus.codelists.graph}";
        String result = PropertyResolver.resolve(input);
        
        assertEquals("http://rdf.insee.fr/graphes/codes-listes", result);
        verify(mockEnvironment).getProperty("fr.insee.rmes.bauhaus.baseGraph", "");
        verify(mockEnvironment).getProperty("fr.insee.rmes.bauhaus.codelists.graph", "");
    }

    @Test
    void shouldHandlePartialResolution() throws Exception {
        setStaticEnvironment(mockEnvironment);
        when(mockEnvironment.getProperty("known.property", "")).thenReturn("known-value");
        when(mockEnvironment.getProperty("unknown.property", "")).thenReturn("");
        
        String input = "${known.property}-${unknown.property}-suffix";
        String result = PropertyResolver.resolve(input);
        
        assertEquals("known-value--suffix", result);
        verify(mockEnvironment).getProperty("known.property", "");
        verify(mockEnvironment).getProperty("unknown.property", "");
    }

    @Test
    void shouldSetEnvironmentThroughSetter() throws Exception {
        propertyResolver.setEnvironment(mockEnvironment);
        
        Environment staticEnv = getStaticEnvironment();
        assertSame(mockEnvironment, staticEnv);
    }


    private void setStaticEnvironment(Environment env) throws Exception {
        Field field = PropertyResolver.class.getDeclaredField("environment");
        field.setAccessible(true);
        field.set(null, env);
    }

    private Environment getStaticEnvironment() throws Exception {
        Field field = PropertyResolver.class.getDeclaredField("environment");
        field.setAccessible(true);
        return (Environment) field.get(null);
    }
}