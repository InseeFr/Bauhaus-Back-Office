package fr.insee.rmes.modules;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import org.springframework.web.bind.annotation.ControllerAdvice;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = { "fr.insee.rmes" }, importOptions = ImportOption.DoNotIncludeTests.class)
public class ForbiddenApiArchTest {

    @ArchTest
    public static final ArchRule noControllerAdvice = FreezingArchRule.freeze(noClasses()
            .should().beAnnotatedWith(ControllerAdvice.class)
            .because("The exception handler should be managed by the controller with a ResponseStatusException exception"));


}
