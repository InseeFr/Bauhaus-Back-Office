package fr.insee.rmes.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import fr.insee.rmes.webservice.response.BaseResponse;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class InfrastructureNamingTest {
    @Test
    void responseNaming() {
        JavaClasses classes = new ClassFileImporter().importPackages("fr.insee.rmes.webservice.response");
        classes().that().areAssignableTo(BaseResponse.class)
                .should().haveSimpleNameEndingWith("Response").check(classes);
    }

}
