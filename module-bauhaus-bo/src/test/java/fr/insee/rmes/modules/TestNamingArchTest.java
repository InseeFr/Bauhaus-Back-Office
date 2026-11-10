package fr.insee.rmes.modules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import org.junit.jupiter.api.Test;

@AnalyzeClasses(packages = "fr.insee.rmes")
public class TestNamingArchTest {

    @ArchTest
    public static final ArchRule test_methods_should_use_snake_case = FreezingArchRule.freeze(
            methods()
                    .that()
                    .areAnnotatedWith(Test.class)
                    .or()
                    .areAnnotatedWith(ArchTest.class)
                    .should()
                    .haveNameMatching("^[a-z][a-z0-9]*(_[a-z0-9]+)*$")
                    .because(
                            "Test methods should use snake_case naming convention (e.g., ma_methode_test) instead of PascalCase (e.g., MaMethodeTest)"));
}
