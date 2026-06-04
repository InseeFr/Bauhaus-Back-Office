package fr.insee.rmes.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import fr.insee.rmes.webservice.response.BaseResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class InfrastructureNamingTest {
    @Test
    void responseNaming() {
        JavaClasses classes = new ClassFileImporter().importPackages("fr.insee.rmes.webservice.response");
        classes().that().areAssignableTo(BaseResponse.class)
                .should().haveSimpleNameEndingWith("Response").check(classes);
    }

    @Test
    void controllerNaming() {
        // Naming/package conventions target production controllers, not test probe controllers.
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("fr.insee.rmes");
        classes().that().areAnnotatedWith(RestController.class)
                .should().haveSimpleNameEndingWith("Resources").check(classes);
    }

    @Test
    void controllerPackageNaming() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("fr.insee.rmes");
        classes().that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..webservice..").check(classes);
    }

}
