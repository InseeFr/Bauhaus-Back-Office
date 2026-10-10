package fr.insee.rmes.testcontainers;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Vide le dataset Fuseki partagé avant chaque classe de tests, pendant de {@code GraphDBResetExtension} :
 * sans ce nettoyage, les données écrites par une classe fuiraient dans la suivante.
 */
public class FusekiResetExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        if (isNested(context)) {
            // Une classe @Nested partage les fixtures de sa classe englobante, déjà chargées.
            return;
        }
        WithFusekiContainer.container.resetTestData();
    }

    private static boolean isNested(ExtensionContext context) {
        return context.getParent().flatMap(ExtensionContext::getTestClass).isPresent();
    }
}
