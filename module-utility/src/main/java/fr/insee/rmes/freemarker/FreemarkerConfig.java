package fr.insee.rmes.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.PlainTextOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;


public class FreemarkerConfig {

    static final Logger logger = LoggerFactory.getLogger(FreemarkerConfig.class);

    /** Le fichier qui déclare les préfixes SPARQL, inséré en tête de chaque requête. */
    private static final String PREFIXES_TEMPLATE = "prefixes.ftlh";

    static Configuration cfg;

    public static void init() {
        // Create your Configuration instance, and specify if up to what FreeMarker
        // version (here 2.3.27) do you want to apply the fixes that are not 100%
        // backward-compatible. See the Configuration JavaDoc for details.
        cfg = new Configuration(Configuration.VERSION_2_3_28);

        // Les templates sont du SPARQL, pas du markup. Sans ces deux réglages, l'extension .ftlh
        // sélectionne le format HTML (recognizeStandardFileExtensions est actif dès la 2.3.24) et
        // FreeMarker échappe chaque ${...} en entités HTML : une apostrophe devient &#39; et la
        // requête ne cherche plus la bonne valeur. L'échappement des valeurs est du ressort de
        // SparqlLiterals, côté Java, qui produit un jeton SPARQL complet.
        cfg.setRecognizeStandardFileExtensions(false);
        cfg.setOutputFormat(PlainTextOutputFormat.INSTANCE);

        // Specify the source where the template files come from. Here I set a
        // plain directory for it, but non-file-system sources are possible too:

        TemplateLoader templateLoader = getTemplateLoader();
        logger.info("Init freemarker templateloader {} , {}", FreemarkerConfig.class.getClassLoader().getResource("request"), FreemarkerConfig.class.getClassLoader().getResource("xdocreport"));
        cfg.setTemplateLoader(templateLoader);

        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.FRANCE);

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
        cfg.setLogTemplateExceptions(false);

        // Wrap unchecked exceptions thrown during template processing into TemplateException-s.
        cfg.setWrapUncheckedExceptions(true);

        // Les préfixes SPARQL sont déclarés une seule fois, dans request/prefixes.ftlh, et FreeMarker
        // les insère en tête de chaque template rendu. Une requête sortie de FreeMarkerUtils est donc
        // exécutable telle quelle : ni concaténation côté Java, ni <#include> à répéter dans les 196
        // templates. Les fragments inclus par un autre template n'héritent pas de l'auto-include, ils
        // le reçoivent via leur hôte.
        cfg.addAutoInclude(PREFIXES_TEMPLATE);


    }

    /**
     * Get template loader
     */
    private static TemplateLoader getTemplateLoader() {
       return new ClassTemplateLoader(FreemarkerConfig.class, "/request");
    }


    public static Configuration getCfg() {
        if (cfg == null) {
            init();
        }
        return cfg;
    }
}
