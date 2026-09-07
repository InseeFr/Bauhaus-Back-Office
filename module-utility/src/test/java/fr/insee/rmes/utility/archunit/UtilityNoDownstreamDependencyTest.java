package fr.insee.rmes.utility.archunit;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "fr.insee.rmes")
class UtilityNoDownstreamDependencyTest {

    @ArchTest
    static final ArchRule no_dependency_on_operation_ddi_or_bauhaus_bo =
            noClasses().should().dependOnClassesThat().resideInAnyPackage(
                    "fr.insee.rmes.modules.operation..",
                    "fr.insee.rmes.modules.ddi..",
                    "fr.insee.rmes.persistance..",
                    "fr.insee.rmes.bauhaus_services..",
                    "fr.insee.rmes.model..",
                    "fr.insee.rmes.exceptions..",
                    "fr.insee.rmes.utils..",
                    "fr.insee.rmes.modules.datasets..",
                    "fr.insee.rmes.modules.clientconfig..",
                    "fr.insee.rmes.modules.users..",
                    "fr.insee.rmes.modules.structures..",
                    "fr.insee.rmes.modules.operations..",
                    "fr.insee.rmes.modules.codeslists..",
                    "fr.insee.rmes.modules.checks..",
                    "fr.insee.rmes.modules.commons..",
                    "fr.insee.rmes.modules.classifications..",
                    "fr.insee.rmes.modules.organisations..",
                    "fr.insee.rmes.modules.geographies..",
                    "fr.insee.rmes.modules.shared_kernel..",
                    "fr.insee.rmes.modules.concepts.."
            );
}
