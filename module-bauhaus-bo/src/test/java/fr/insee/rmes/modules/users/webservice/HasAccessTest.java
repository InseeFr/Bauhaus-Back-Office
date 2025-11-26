package fr.insee.rmes.modules.users.webservice;

import fr.insee.rmes.modules.users.domain.model.RBAC;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class HasAccessTest {

    @Test
    void shouldHaveCorrectRetentionPolicy() {
        Retention retention = HasAccess.class.getAnnotation(Retention.class);

        assertThat(retention).isNotNull();
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
    }

    @Test
    void shouldBeApplicableToMethodsAndTypes() {
        var target = HasAccess.class.getAnnotation(java.lang.annotation.Target.class);

        assertThat(target).isNotNull();
        assertThat(target.value()).contains(ElementType.METHOD, ElementType.TYPE);
    }

    @Test
    void shouldBeAnnotatedWithPreAuthorize() {
        PreAuthorize preAuthorize = HasAccess.class.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value())
            .contains("@propertiesAccessPrivilegesChecker.hasAccess")
            .contains("{module}")
            .contains("{privilege}")
            .contains("#id")
            .contains("authentication.principal");
    }

    @Test
    void shouldHaveModuleAttribute() throws NoSuchMethodException {
        Method moduleMethod = HasAccess.class.getMethod("module");

        assertThat(moduleMethod).isNotNull();
        assertThat(moduleMethod.getReturnType()).isEqualTo(RBAC.Module.class);
    }

    @Test
    void shouldHavePrivilegeAttribute() throws NoSuchMethodException {
        Method privilegeMethod = HasAccess.class.getMethod("privilege");

        assertThat(privilegeMethod).isNotNull();
        assertThat(privilegeMethod.getReturnType()).isEqualTo(RBAC.Privilege.class);
    }

    @Test
    void shouldBeUsableOnMethod() {
        // Create a test class with the annotation
        class TestController {
            @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.READ)
            public void testMethod() {
            }
        }

        Method method = TestController.class.getDeclaredMethods()[0];
        HasAccess annotation = method.getAnnotation(HasAccess.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.module()).isEqualTo(RBAC.Module.CONCEPT_CONCEPT);
        assertThat(annotation.privilege()).isEqualTo(RBAC.Privilege.READ);
    }

    @Test
    void shouldBeUsableOnType() {
        @HasAccess(module = RBAC.Module.OPERATION_SERIES, privilege = RBAC.Privilege.CREATE)
        class TestController {
        }

        HasAccess annotation = TestController.class.getAnnotation(HasAccess.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.module()).isEqualTo(RBAC.Module.OPERATION_SERIES);
        assertThat(annotation.privilege()).isEqualTo(RBAC.Privilege.CREATE);
    }

    @Test
    void shouldSupportDifferentModules() {
        class TestController {
            @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.UPDATE)
            public void datasetMethod() {}

            @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.DELETE)
            public void structureMethod() {}

            @HasAccess(module = RBAC.Module.CLASSIFICATION_FAMILY, privilege = RBAC.Privilege.PUBLISH)
            public void classificationMethod() {}
        }

        Method[] methods = TestController.class.getDeclaredMethods();

        assertThat(methods).hasSize(3);
        for (Method method : methods) {
            HasAccess annotation = method.getAnnotation(HasAccess.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.module()).isNotNull();
            assertThat(annotation.privilege()).isNotNull();
        }
    }

    @Test
    void shouldSupportAllPrivilegeTypes() {
        class TestController {
            @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.CREATE)
            public void createMethod() {}

            @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.READ)
            public void readMethod() {}

            @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.UPDATE)
            public void updateMethod() {}

            @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.DELETE)
            public void deleteMethod() {}

            @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.PUBLISH)
            public void publishMethod() {}

            @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.ADMINISTRATION)
            public void adminMethod() {}
        }

        Method[] methods = TestController.class.getDeclaredMethods();

        assertThat(methods).hasSize(6);
        for (Method method : methods) {
            HasAccess annotation = method.getAnnotation(HasAccess.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.privilege()).isIn(
                RBAC.Privilege.CREATE,
                RBAC.Privilege.READ,
                RBAC.Privilege.UPDATE,
                RBAC.Privilege.DELETE,
                RBAC.Privilege.PUBLISH,
                RBAC.Privilege.ADMINISTRATION
            );
        }
    }

    @Test
    void shouldAllowCombinationOfAllModulesAndPrivileges() {
        // Test a representative sample of module/privilege combinations
        class TestController {
            @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.CREATE)
            public void method1() {}

            @HasAccess(module = RBAC.Module.OPERATION_SERIES, privilege = RBAC.Privilege.UPDATE)
            public void method2() {}

            @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.DELETE)
            public void method3() {}

            @HasAccess(module = RBAC.Module.GEOGRAPHY, privilege = RBAC.Privilege.READ)
            public void method4() {}
        }

        for (Method method : TestController.class.getDeclaredMethods()) {
            HasAccess annotation = method.getAnnotation(HasAccess.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.module()).isInstanceOf(RBAC.Module.class);
            assertThat(annotation.privilege()).isInstanceOf(RBAC.Privilege.class);
        }
    }
}
