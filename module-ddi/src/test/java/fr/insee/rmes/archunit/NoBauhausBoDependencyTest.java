package fr.insee.rmes.archunit;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "fr.insee.rmes.modules.ddi")
class NoBauhausBoDependencyTest {

    /**
     * Les annotations d'architecture hexagonale portent le préfixe {@code fr.insee.rmes.modules.commons}
     * mais vivent dans {@code module-utility}, dépendance légitime de {@code module-ddi} : ce ne sont pas
     * des classes de {@code module-bauhaus-bo}, elles échappent donc à l'interdiction.
     */
    private static final DescribedPredicate<JavaClass> HEXAGONAL_ANNOTATIONS =
            resideInAPackage("fr.insee.rmes.modules.commons.hexagonal..");

    private static final DescribedPredicate<JavaClass> BAUHAUS_BO_PACKAGES = resideInAnyPackage(
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
            "fr.insee.rmes.modules.concepts..");

    @ArchTest
    static final ArchRule no_dependency_on_bauhaus_bo_packages =
            noClasses().should().dependOnClassesThat(BAUHAUS_BO_PACKAGES.and(not(HEXAGONAL_ANNOTATIONS)));
}
