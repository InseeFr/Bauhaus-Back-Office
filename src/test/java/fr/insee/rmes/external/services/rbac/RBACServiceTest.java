package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.config.RBACTest;
import fr.insee.rmes.config.auth.RBACConfiguration;
import fr.insee.rmes.model.rbac.Module;
import fr.insee.rmes.model.rbac.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.insee.rmes.model.rbac.Module.CONCEPT;
import static fr.insee.rmes.model.rbac.Privilege.*;
import static fr.insee.rmes.model.rbac.Strategy.ALL;
import static fr.insee.rmes.model.rbac.Strategy.STAMP;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

@RBACTest
class RBACServiceTest {

    @Autowired
    RBACService rbacService;

    @Test
    void shouldReturnAnEmptyMapIfMissingRoles() {
        ApplicationAccessPrivileges applicationAccessPrivileges = rbacService.computeRbac(List.of());
        assertThat(applicationAccessPrivileges).isEqualTo(ApplicationAccessPrivileges.NO_PRIVILEGE);
    }

    @Test
    void shouldReturnAnEmptyMapIfUnknownRole() {
        ApplicationAccessPrivileges applicationAccessPrivileges = rbacService.computeRbac(List.of(new RBACConfiguration.RoleName("UNKNOWN")));
        assertThat(applicationAccessPrivileges).isEqualTo(ApplicationAccessPrivileges.NO_PRIVILEGE);
    }

    @Test
    void shouldReturnAllReadWithUserRole() {
        checkCrossModulesWithPrivilegeForStrategyRegardingRole("Utilisateur_RMESGNCS", Module.values(), new Privilege[]{READ}, ALL);
    }

    private void checkCrossModulesWithPrivilegeForStrategyRegardingRole(String role, Module[] modules, Privilege[] privileges, Strategy strategy) {
        ApplicationAccessPrivileges applicationAccessPrivileges = rbacService.computeRbac(List.of(new RBACConfiguration.RoleName(role)));
        var expected = new ApplicationAccessPrivileges(new EnumMap<>(Arrays.stream(modules).collect(Collectors.toMap(
                Function.identity(),
                ignored-> new ModuleAccessPrivileges(Arrays.stream(privileges).collect(Collectors.toMap(
                        Function.identity(),
                        ignoredAgain -> strategy,
                        Strategy::merge
                ))),
                ModuleAccessPrivileges::merge
        ))));
        assertThat(applicationAccessPrivileges).isEqualTo(expected);
    }

    @Test
    void shouldReturnNeverWriteWithUserRole() {
        ApplicationAccessPrivileges applicationAccessPrivileges = rbacService.computeRbac(List.of(new RBACConfiguration.RoleName("Utilisateur_RMESGNCS")));
        assertThat(allPrivileges(applicationAccessPrivileges)).isNotEmpty().allMatch(READ::equals);
    }

    private static Stream<Privilege> allPrivileges(ApplicationAccessPrivileges applicationAccessPrivileges) {
        return applicationAccessPrivileges.privilegesByModules().values().stream().flatMap(map -> map.strategysByPrivileges().keySet().stream());
    }

    @Test
    void shouldReturnAllPrivilegeWithAdminRole() {
        checkCrossModulesWithPrivilegeForStrategyRegardingRole("Administrateur_RMESGNCS", Module.values(), Privilege.values(), ALL);
    }

    @Test
    void shouldReturnStampPrivilegeForConceptsWithGestionnaireConcept() {
        checkCrossModulesWithPrivilegeForStrategyRegardingRole("Gestionnaire_concept_RMESGNCS", new Module[]{CONCEPT}, new Privilege[]{CREATE, READ, PUBLISH, UPDATE, VALIDATE}, STAMP);
    }

    @Test
    void testMergeAdminAndSimpleUserShouldReturnAdmin(){
        var adminPrivilege = rbacService.computeRbac(List.of(new RBACConfiguration.RoleName("Administrateur_RMESGNCS")));
        var mergedPrivilege = rbacService.computeRbac(List.of(new RBACConfiguration.RoleName("Utilisateur_RMESGNCS"), new RBACConfiguration.RoleName("Administrateur_RMESGNCS")));
        var mergedPrivilegeReversed = rbacService.computeRbac(List.of(new RBACConfiguration.RoleName("Administrateur_RMESGNCS"), new RBACConfiguration.RoleName("Utilisateur_RMESGNCS")));
        assertThat(mergedPrivilege).isEqualTo(adminPrivilege);
        assertThat(mergedPrivilegeReversed).isEqualTo(adminPrivilege);
    }

    @Test
    void testMergeGestionnaireAndSimpleUserShouldReturnMix(){
        var mergedPrivilege = rbacService.computeRbac(List.of(new RBACConfiguration.RoleName("Utilisateur_RMESGNCS"), new RBACConfiguration.RoleName("Gestionnaire_concept_RMESGNCS")));
        var mergedPrivilegeReversed = rbacService.computeRbac(List.of(new RBACConfiguration.RoleName("Gestionnaire_concept_RMESGNCS"), new RBACConfiguration.RoleName("Utilisateur_RMESGNCS")));
        var allModulesExceptConcept = Arrays.stream(Module.values()).filter(not(CONCEPT::equals)).toList();
        assertThat(allModulesExceptConcept.stream().map(mergedPrivilege::privilegesForModule).flatMap(m->m.strategysByPrivileges().keySet().stream())).isNotEmpty().allMatch(READ::equals);
        assertThat(allModulesExceptConcept.stream().map(mergedPrivilegeReversed::privilegesForModule).flatMap(m->m.strategysByPrivileges().keySet().stream())).isNotEmpty().allMatch(READ::equals);
        List<Privilege> writePrivilege=List.of(PUBLISH, UPDATE, VALIDATE);
        ModuleAccessPrivileges moduleAccessPrivilegesReversed = mergedPrivilegeReversed.privilegesForModule(CONCEPT);
        assertThat(writePrivilege.stream().map(moduleAccessPrivilegesReversed::strategyFor)).isNotEmpty().allMatch(Optional.of(STAMP)::equals);
        //according to https://github.com/InseeFr/Bauhaus-Back-Office/issues/634#issuecomment-2286303989
        assertThat(moduleAccessPrivilegesReversed.strategyFor(CREATE)).isPresent().isEqualTo(Optional.of(STAMP));
        assertThat(moduleAccessPrivilegesReversed.strategyFor(READ)).isEqualTo(Optional.of(ALL));

        ModuleAccessPrivileges moduleAccessPrivileges = mergedPrivilege.privilegesForModule(CONCEPT);
        assertThat(writePrivilege.stream().map(moduleAccessPrivileges::strategyFor)).isNotEmpty().allMatch(Optional.of(STAMP)::equals);
        //according to https://github.com/InseeFr/Bauhaus-Back-Office/issues/634#issuecomment-2286303989
        assertThat(moduleAccessPrivileges.strategyFor(CREATE)).isPresent().isEqualTo(Optional.of(STAMP));
        assertThat(moduleAccessPrivileges.strategyFor(READ)).isPresent().isEqualTo(Optional.of(ALL));
    }

}