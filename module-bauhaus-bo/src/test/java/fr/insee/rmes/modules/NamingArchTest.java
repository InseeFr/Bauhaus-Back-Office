package fr.insee.rmes.modules;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = { "fr.insee.rmes.modules" }, importOptions = ImportOption.DoNotIncludeTests.class)
public class NamingArchTest {

    @ArchTest
    public static final ArchRule exceptionNaming = classes()
            .that().resideInAPackage("..domain..")
            .and().areAssignableTo(Exception.class)
            .should().haveSimpleNameEndingWith("Exception")
            .because("Domain exceptions should be named with 'Exception' suffix");


}
