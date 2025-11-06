package fr.insee.rmes.modules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "fr.insee.rmes.modules.concepts.collections", importOptions = ImportOption.DoNotIncludeTests.class)
public class HexagonaleArchTest {
    @ArchTest
    public static final ArchRule domainDependencies = FreezingArchRule.freeze(classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("..domain..", "java..")
            .because("The domain should only depends of the domain"));

    @ArchTest
    public static final ArchRule webServiceNaming = classes().that().areAnnotatedWith(RestController.class)
                .should().haveSimpleNameEndingWith("Resources");

    @ArchTest
    public static final ArchRule webServicePackageName = classes().that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..webservice..");

    @ArchTest
    public static final ArchRule webServicePackageDependendencies = noClasses()
            .that().resideInAPackage("..webservice..")
            .should().dependOnClassesThat().resideInAnyPackage("..serverside..", "..infrastructure..")
            .because("The webservices should not depends of the serverside ports or the infrastructure ");

    @ArchTest
    public static final ArchRule infrastructurePackageDependendencies = noClasses()
            .that().resideInAPackage("..infrastructure..")
            .should().dependOnClassesThat().resideInAnyPackage("..webservice..", "..clientside..")
            .because("The infrastructure should not depends of the clientside ports or the webservice ");

    // verifier qu'un port clientside defini dans le domain soit implementer dans le package domain


    // verifier qu'un port serverside defini dans le domain soit implementer dans le package infra
}
