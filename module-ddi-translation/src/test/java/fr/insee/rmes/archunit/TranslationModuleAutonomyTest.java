package fr.insee.rmes.archunit;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Garantit l'autonomie du module de traduction : la traduction DDI3 &harr; DDI4 ne doit dependre
 * ni des autres modules metier (utility, operation), ni de l'infrastructure Colectica ou des ports
 * serverside qui restent dans {@code module-ddi}, ni du back-office. Le pom n'expose deja aucune de
 * ces dependances ; ce test verrouille la regression.
 */
@AnalyzeClasses(packages = "fr.insee.rmes.modules.ddi.physical_instances")
class TranslationModuleAutonomyTest {

    @ArchTest
    static final ArchRule translation_stays_autonomous =
            noClasses().should().dependOnClassesThat().resideInAnyPackage(
                    "fr.insee.rmes.modules.utility..",
                    "fr.insee.rmes.modules.operation..",
                    "fr.insee.rmes.modules.ddi.physical_instances.infrastructure..",
                    "fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside..",
                    "fr.insee.rmes.persistance..",
                    "fr.insee.rmes.bauhaus_services..",
                    "fr.insee.rmes.modules.users..",
                    "fr.insee.rmes.modules.operations.."
            );
}
