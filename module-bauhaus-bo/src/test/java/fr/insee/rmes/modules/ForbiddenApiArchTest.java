package fr.insee.rmes.modules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noCodeUnits;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import fr.insee.rmes.modules.commons.webservice.ValidationExceptionHandler;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;

@AnalyzeClasses(
        packages = {"fr.insee.rmes"},
        importOptions = ImportOption.DoNotIncludeTests.class)
public class ForbiddenApiArchTest {

    private static final String LG1_PROPERTY = "fr.insee.rmes.bauhaus.lg1";
    private static final String LG2_PROPERTY = "fr.insee.rmes.bauhaus.lg2";

    /**
     * {@link ValidationExceptionHandler} est explicitement exempté : le format d'erreur de la
     * validation des corps de requête est transverse par nature, aucun contrôleur ne peut le rendre
     * lui-même avec une {@code ResponseStatusException}. Les autres handlers restent de la dette,
     * gelée dans {@code archunit_store}.
     */
    @ArchTest
    public static final ArchRule noControllerAdvice = FreezingArchRule.freeze(
            noClasses()
                    .that()
                    .areNotAssignableTo(ValidationExceptionHandler.class)
                    .should()
                    .beMetaAnnotatedWith(ControllerAdvice.class)
                    .because(
                            "The exception handler should be managed by the controller with a ResponseStatusException exception"));

    @ArchTest
    public static final ArchRule noLanguagePropertyInjectedWithValue = noCodeUnits()
            .should(injectALanguagePropertyWithValue())
            .because(
                    "two String parameters can be swapped without the compiler noticing: inject the BauhausLanguagesProperties object instead");

    private static ArchCondition<JavaCodeUnit> injectALanguagePropertyWithValue() {
        return new ArchCondition<>("inject " + LG1_PROPERTY + " or " + LG2_PROPERTY + " with @Value") {
            @Override
            public void check(JavaCodeUnit codeUnit, ConditionEvents events) {
                for (JavaParameter parameter : codeUnit.getParameters()) {
                    parameter.getAnnotations().stream()
                            .filter(annotation -> annotation.getRawType().isAssignableTo(Value.class))
                            .map(ForbiddenApiArchTest::expressionOf)
                            .flatMap(Optional::stream)
                            .filter(expression ->
                                    expression.contains(LG1_PROPERTY) || expression.contains(LG2_PROPERTY))
                            .forEach(expression -> events.add(SimpleConditionEvent.satisfied(
                                    codeUnit, codeUnit.getFullName() + " injects " + expression)));
                }
            }
        };
    }

    private static Optional<String> expressionOf(JavaAnnotation<? extends JavaParameter> annotation) {
        return annotation.get("value").map(String::valueOf);
    }
}
