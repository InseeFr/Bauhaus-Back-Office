package fr.insee.rmes.freemarker;

import fr.insee.rmes.domain.exceptions.RmesException;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FreemarkerConfigTest {

    /**
     * Les templates sont du SPARQL, pas du HTML : une valeur interpolée doit sortir telle quelle.
     * L'échappement de la valeur relève de SparqlLiterals, côté Java.
     */
    @Test
    void shouldNotEscapeAnInterpolatedValueInAFtlhTemplate() throws RmesException {
        String rendered = FreeMarkerUtils.buildRequest("", "escapingProbe.ftlh",
                Map.of("VALUE", "a\"b'c&d<e>"));

        assertEquals("VALUES ?x { a\"b'c&d<e> }", rendered.trim());
    }

    @Test
    void shouldReturnConfigurationInstance() {
        Configuration config = FreemarkerConfig.getCfg();
        assertNotNull(config);
    }

    @Test
    void shouldReturnSameConfigurationInstanceOnMultipleCalls() {
        Configuration config1 = FreemarkerConfig.getCfg();
        Configuration config2 = FreemarkerConfig.getCfg();
        assertSame(config1, config2);
    }

    @Test
    void shouldHaveCorrectDefaultEncoding() {
        Configuration config = FreemarkerConfig.getCfg();
        assertEquals("UTF-8", config.getDefaultEncoding());
    }

    @Test
    void shouldHaveCorrectLocale() {
        Configuration config = FreemarkerConfig.getCfg();
        assertEquals(Locale.FRANCE, config.getLocale());
    }

    @Test
    void shouldHaveCorrectTemplateExceptionHandler() {
        Configuration config = FreemarkerConfig.getCfg();
        assertEquals(TemplateExceptionHandler.RETHROW_HANDLER, config.getTemplateExceptionHandler());
    }

    @Test
    void shouldHaveCorrectLogTemplateExceptionsSettings() {
        Configuration config = FreemarkerConfig.getCfg();
        assertFalse(config.getLogTemplateExceptions());
    }

    @Test
    void shouldHaveCorrectWrapUncheckedExceptionsSettings() {
        Configuration config = FreemarkerConfig.getCfg();
        assertTrue(config.getWrapUncheckedExceptions());
    }

    @Test
    void shouldHaveTemplateLoaderConfigured() {
        Configuration config = FreemarkerConfig.getCfg();
        assertNotNull(config.getTemplateLoader());
    }

    @Test
    void shouldHaveCorrectVersion() {
        Configuration config = FreemarkerConfig.getCfg();
        assertEquals(Configuration.VERSION_2_3_28, config.getIncompatibleImprovements());
    }
}