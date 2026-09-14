package fr.insee.rmes.modules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Un corps de requête doit être validé par Bean Validation, pas par des {@code if} dispersés
 * dans les contrôleurs et les services.
 * <p>
 * La règle est gelée : les endpoints pas encore migrés (pour l'essentiel des
 * {@code @RequestBody String} bruts) sont enregistrés dans {@code archunit_store} et tolérés,
 * mais aucun nouvel endpoint ne peut s'ajouter sans {@code @Valid}. Le store ne doit que décroître,
 * lot après lot.
 *
 * @see ArchUnitStoreTest
 */
@AnalyzeClasses(packages = "fr.insee.rmes", importOptions = ImportOption.DoNotIncludeTests.class)
public class ValidationArchTest {

    @ArchTest
    public static final ArchRule requestBodiesShouldBeValidated = FreezingArchRule.freeze(methods()
            .that(areWriteMappings())
            .should(declareValidOnEveryRequestBody())
            .because("A request body must be validated by Bean Validation (@Valid + constraints on the DTO), "
                    + "so that a rejected payload answers the contractual 400 {errors:[{field,message}]}"));

    private static DescribedPredicate<JavaMethod> areWriteMappings() {
        return new DescribedPredicate<>("are annotated with @PostMapping, @PutMapping or @PatchMapping") {
            @Override
            public boolean test(JavaMethod method) {
                return method.isAnnotatedWith(PostMapping.class)
                        || method.isAnnotatedWith(PutMapping.class)
                        || method.isAnnotatedWith(PatchMapping.class);
            }
        };
    }

    private static ArchCondition<JavaMethod> declareValidOnEveryRequestBody() {
        return new ArchCondition<>("declare @Valid on every @RequestBody parameter") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                for (JavaParameter parameter : method.getParameters()) {
                    if (parameter.isAnnotatedWith(RequestBody.class) && !parameter.isAnnotatedWith(Valid.class)) {
                        events.add(SimpleConditionEvent.violated(
                                method,
                                method.getFullName() + " takes a @RequestBody that is not annotated with @Valid"));
                    }
                }
            }
        };
    }
}
