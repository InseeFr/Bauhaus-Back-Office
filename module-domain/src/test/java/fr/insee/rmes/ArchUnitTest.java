package fr.insee.rmes;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class ArchUnitTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(location -> !location.contains("/test-classes/"))
            .withImportOption(location -> !location.contains("exceptions"))
            .importPackages("fr.insee.rmes");

    @Test
    void domainModelShouldOnlyDependOnAllowedPackages() {
        ArchRule rule = classes()
            .that().resideInAPackage("fr.insee.rmes.domain.model..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                "fr.insee.rmes.domain..",
                "java..",
                "org.json.."
            );

        rule.check(importedClasses);
    }

    @Test
    void domainServicesShouldOnlyDependOnAllowedPackages() {
        ArchRule rule = classes()
            .that().resideInAPackage("fr.insee.rmes.domain.services..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                "fr.insee.rmes.domain..",
                "java..",
                "org.slf4j.."
            );

        rule.check(importedClasses);
    }
}
