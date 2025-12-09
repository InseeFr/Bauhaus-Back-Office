package fr.insee.rmes.modules;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = {
        "fr.insee.rmes.modules.concepts.collections",
        "fr.insee.rmes.modules.users"
}, importOptions = ImportOption.DoNotIncludeTests.class)
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
            .should().onlyDependOnClassesThat().resideInAnyPackage("..domain..", "java..", "org.apache.commons.lang3..", "org.jspecify.annotations..", "org.slf4j..", "fr.insee.rmes.modules.commons.hexagonal..")
            .because("The domain should only depends of the domain"));

    @ArchTest
    public static final ArchRule webServiceNaming = classes().that().areAnnotatedWith(RestController.class)
                .should().haveSimpleNameEndingWith("Resources")
                .andShould().resideInAPackage("..webservice..");

    @ArchTest
    public static final ArchRule serverSideAdaptorShouldImplementServerSidePort = classes()
            .that().areAnnotatedWith(ServerSideAdaptor.class)
            .should().resideInAPackage("..infrastructure..")
            .andShould().implement(new DescribedPredicate<JavaClass>("Check if the interface is annotated with ServerSidePort") {
                @Override
                public boolean test(JavaClass javaClass) {
                    return javaClass.isAnnotatedWith(ServerSidePort.class);
                }
            });


    @ArchTest
    public static final ArchRule webServicePackageDependendencies = noClasses()
            .that().resideInAPackage("..webservice..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..")
            .orShould().dependOnClassesThat().areAnnotatedWith(ServerSidePort.class)
            .because("The webservices should not depends of the serverside ports or the infrastructure ");

    @ArchTest
    public static final ArchRule infrastructurePackageDependendencies = noClasses()
            .that().resideInAPackage("..infrastructure..")
            .should().dependOnClassesThat().resideInAnyPackage("..webservice..")
            .orShould().dependOnClassesThat().areAnnotatedWith(ClientSidePort.class)
            .because("The infrastructure should not depends of the clientside ports or the webservice ");

    @ArchTest
    public static final ArchRule clientsidePortsImplementedInDomain = classes()
            .that().areAnnotatedWith(ClientSidePort.class)
            .should().resideInAPackage("..clientside..")
            .andShould().beInterfaces()
            .andShould().haveSimpleNameEndingWith("Service")
            .andShould(beImplementedIn(".domain.", "Domain"))
            .because("All clientside ports defined in domain should be implemented in domain package with 'Domain' prefix");

    @ArchTest
    public static final ArchRule onlyClientSideInterfaceInsideClientSidePackage = classes()
            .that().resideInAPackage("..clientside..")
            .should().beAnnotatedWith(ClientSidePort.class)
            .because("The package clientside should only contain Client Side port");

    @ArchTest
    public static final ArchRule serversidePortsImplementedInInfrastructure = classes()
            .that().areAnnotatedWith(ServerSidePort.class)
            .should().resideInAPackage("..serverside..")
            .andShould().beInterfaces()
            .andShould(beImplementedIn(".infrastructure.", ""))
            .because("All serverside ports defined in domain should be implemented in infrastructure package");

    @ArchTest
    public static final ArchRule onlyServerSidePortInsideServerSidePackage = classes()
            .that().resideInAPackage("..serverside..")
            .should().beAnnotatedWith(ServerSidePort.class)
            .because("The package serverside should only contain Server Side port");



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

}
