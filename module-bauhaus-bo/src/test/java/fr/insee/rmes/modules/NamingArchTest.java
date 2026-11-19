package fr.insee.rmes.modules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import fr.insee.rmes.modules.commons.webservice.BaseResponse;

@AnalyzeClasses(
        packages = {"fr.insee.rmes"},
        importOptions = ImportOption.DoNotIncludeTests.class)
public class NamingArchTest {

    @ArchTest
    public static final ArchRule exceptionNaming = classes()
            .that()
            .resideInAPackage("..domain..")
            .and()
            .areAssignableTo(Exception.class)
            .should()
            .haveSimpleNameEndingWith("Exception")
            .because("Domain exceptions should be named with 'Exception' suffix");

    @ArchTest
    public static final ArchRule noUtilsClasses = FreezingArchRule.freeze(noClasses()
            .that()
            .resideOutsideOfPackage("fr.insee.rmes.utils..")
            .should()
            .haveSimpleNameEndingWith("Utils")
            .because("Une classe nommée *Utils masque sa responsabilité : nommer selon le rôle "
                    + "(Repository, Mapper, Publisher…). Les boîtes à outils statiques restent "
                    + "dans fr.insee.rmes.utils"));

    @ArchTest
    public static final ArchRule responseNaming =
            classes().that().areAssignableTo(BaseResponse.class).should().haveSimpleNameEndingWith("Response");
}
