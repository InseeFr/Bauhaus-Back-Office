package fr.insee.rmes.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
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
        JavaClasses classes = new ClassFileImporter().importPackages("fr.insee.rmes");
        classes().that().areAnnotatedWith(RestController.class)
                .should().haveSimpleNameEndingWith("Resources").check(classes);
    }

    @Test
    void controllerPackageNaming() {
        JavaClasses classes = new ClassFileImporter().importPackages("fr.insee.rmes");
        classes().that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..webservice..").check(classes);
    }

}
