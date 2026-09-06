package fr.insee.rmes.testcontainers;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Vide le triplestore partagé avant chaque classe de tests.
 *
 * <p>C'est ce qui remplace le redémarrage du conteneur : sans ce nettoyage, les données écrites par
 * une classe fuiraient dans la suivante. Le hook est un {@link BeforeAllCallback} et non un
 * {@code @BeforeAll} car il doit s'exécuter avant les deux endroits où les tests chargent leurs
 * fixtures — les méthodes {@code @BeforeAll} des classes filles, et les {@code @DynamicPropertySource}
 * évalués plus tard encore, à la création du contexte Spring.
 */
public class GraphDBResetExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        if (isNested(context)) {
            // Une classe @Nested partage les fixtures de sa classe englobante, déjà chargées par le
            // @BeforeAll de celle-ci : la purger ici viderait la base sous les pieds des tests.
            return;
        }
        WithGraphDBContainer.container.resetTestData();
    }

    private static boolean isNested(ExtensionContext context) {
        return context.getParent().flatMap(ExtensionContext::getTestClass).isPresent();
    }
}
