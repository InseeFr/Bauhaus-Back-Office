package fr.insee.rmes.freemarker;

import static org.junit.jupiter.api.Assertions.*;

import fr.insee.rmes.domain.exceptions.RmesException;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FreemarkerConfigTest {

    /**
     * Les templates sont du SPARQL, pas du HTML : une valeur interpolée doit sortir telle quelle.
     * L'échappement de la valeur relève de SparqlLiterals, côté Java.
     */
    @Test
    void shouldNotEscapeAnInterpolatedValueInAFtlhTemplate() throws RmesException {
        String rendered = FreeMarkerUtils.buildRequest("", "escapingProbe.ftlh", Map.of("VALUE", "a\"b'c&d<e>"));

        assertTrue(rendered.trim().endsWith("VALUES ?x { a\"b'c&d<e> }"), () -> rendered);
    }

    /**
     * Les préfixes SPARQL sont déclarés une seule fois, dans {@code request/prefixes.ftlh}, et
     * FreeMarker les insère en tête de chaque requête rendue (auto-include). Plus aucune
     * concaténation côté Java : une requête sortie de {@code buildRequest} est exécutable telle quelle.
     */
    @Test
    void shouldPrependTheSparqlPrefixesToEveryRenderedQuery() throws RmesException {
        String rendered = FreeMarkerUtils.buildRequest("", "escapingProbe.ftlh", Map.of("VALUE", "?x"));

        assertTrue(rendered.startsWith("PREFIX "), () -> "les préfixes manquent en tête de :\n" + rendered);
        assertTrue(rendered.contains("PREFIX dcterms:<http://purl.org/dc/terms/>"));
        assertTrue(rendered.contains("PREFIX skos:<http://www.w3.org/2004/02/skos/core#>"));
        assertTrue(rendered.contains("PREFIX insee:<http://rdf.insee.fr/def/base#>"));
        assertTrue(rendered.contains("PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>"));
    }

    /**
     * owl: est utilisé par les requêtes ; il doit être déclaré et non hérité des namespaces du
     * triplestore (régression : getUriClasseOwl cassait en production).
     */
    @Test
    void shouldDeclareTheOwlPrefixUsedByTheTemplates() throws RmesException {
        String rendered = FreeMarkerUtils.buildRequest("", "escapingProbe.ftlh", Map.of("VALUE", "?x"));

        assertTrue(rendered.contains("PREFIX owl:<http://www.w3.org/2002/07/owl#>"));
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
