package fr.insee.rmes.freemarker;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class FreemarkerConfigTest {

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