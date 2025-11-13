package fr.insee.rmes.modules;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import fr.insee.rmes.modules.commons.configuration.conditional.ConditionalOnModule;
import fr.insee.rmes.rbac.HasAccess;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "fr.insee.rmes.modules.concepts.collections", importOptions = ImportOption.DoNotIncludeTests.class)
public class HexagonaleArchTest {

    private static ArchCondition<JavaClass> beImplementedIn(String targetPackage, String prefix) {
        String description = prefix != null
                ? "be implemented in " + targetPackage + " package with prefix '" + prefix + "'"
                : "be implemented in " + targetPackage + " package";

        return new ArchCondition<JavaClass>(description) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                var implementations = javaClass.getAllSubclasses().stream()
                        .filter(subClass -> {
                            String packageName = subClass.getPackage().getName();
                            // Check if package contains targetPackage or ends with it
                            return packageName.contains(targetPackage) ||
                                   packageName.endsWith(targetPackage.replace(".", ""));
                        })
                        .toList();

                if (implementations.isEmpty()) {
                    String message = String.format(
                            "Port %s has no implementation in %s package",
                            javaClass.getFullName(),
                            targetPackage
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                    return;
                }

                // Check prefix if specified
                if (prefix != null) {
                    for (JavaClass implementation : implementations) {
                        if (!implementation.getSimpleName().startsWith(prefix)) {
                            String message = String.format(
                                    "Implementation %s does not start with prefix '%s'",
                                    implementation.getFullName(),
                                    prefix
                            );
                            events.add(SimpleConditionEvent.violated(implementation, message));
                        }
                    }
                }
            }
        };
    }

    @ArchTest
    public static final ArchRule domainDependencies = FreezingArchRule.freeze(classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("..domain..", "java..", "org.apache.commons.lang3..", "org.jspecify.annotations..")
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

    @ArchTest
    public static final ArchRule clientsidePortsImplementedInDomain = classes()
            .that().resideInAPackage("..clientside..")
            .and().areInterfaces()
            .should(beImplementedIn(".domain.", "Domain"))
            .because("All clientside ports defined in domain should be implemented in domain package with 'Domain' prefix");

    @ArchTest
    public static final ArchRule serversidePortsImplementedInInfrastructure = classes()
            .that().resideInAPackage("..serverside..")
            .and().areInterfaces()
            .should(beImplementedIn(".infrastructure.", null))
            .because("All serverside ports defined in domain should be implemented in infrastructure package");

    @ArchTest
    public static final ArchRule repositoryNaming = classes()
            .that().resideInAPackage("..serverside..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("Repository")
            .because("Serverside ports should be named with 'Repository' suffix");

    @ArchTest
    public static final ArchRule serviceNaming = classes()
            .that().resideInAPackage("..clientside..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("Service")
            .because("Clientside ports should be named with 'Service' suffix");

    @ArchTest
    public static final ArchRule exceptionNaming = classes()
            .that().resideInAPackage("..domain..")
            .and().areAssignableTo(Exception.class)
            .should().haveSimpleNameEndingWith("Exception")
            .because("Domain exceptions should be named with 'Exception' suffix");

    @ArchTest
    public static final ArchRule configurationNaming = classes()
            .that().areAnnotatedWith(Configuration.class)
            .should().haveSimpleNameEndingWith("Configuration")
            .because("Spring configuration classes should be named with 'Configuration' suffix");

    // Port structure

    @ArchTest
    public static final ArchRule portsShouldBeInterfaces = FreezingArchRule.freeze(classes()
            .that().resideInAPackage("..port..")
            .should().beInterfaces()
            .because("All ports should be interfaces to define contracts"));

    // RestController annotations

    @ArchTest
    public static final ArchRule restControllerShouldHaveSecurityRequirement = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().beAnnotatedWith(SecurityRequirement.class)
            .because("All RestController classes should be annotated with @SecurityRequirement for security documentation");

    @ArchTest
    public static final ArchRule restControllerShouldHaveConditionalOnExpression = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().beAnnotatedWith(ConditionalOnModule.class)
            .because("All RestController classes should be annotated with @ConditionalOnExpression to enable/disable modules");

    // HTTP methods annotations

    @ArchTest
    public static final ArchRule httpMethodsShouldHaveHasAccess = methods()
            .that().areAnnotatedWith(PostMapping.class)
            .or().areAnnotatedWith(GetMapping.class)
            .or().areAnnotatedWith(PutMapping.class)
            .or().areAnnotatedWith(PatchMapping.class)
            .or().areAnnotatedWith(DeleteMapping.class)
            .should().beAnnotatedWith(HasAccess.class)
            .because("All HTTP endpoint methods should be annotated with @HasAccess for authorization control");

    @ArchTest
    public static final ArchRule httpMethodsShouldReturnResponseEntity = methods()
            .that().areAnnotatedWith(PostMapping.class)
            .or().areAnnotatedWith(GetMapping.class)
            .or().areAnnotatedWith(PutMapping.class)
            .or().areAnnotatedWith(PatchMapping.class)
            .or().areAnnotatedWith(DeleteMapping.class)
            .should().haveRawReturnType(ResponseEntity.class)
            .because("All HTTP endpoint methods should return ResponseEntity for consistent error handling and status codes");
}
