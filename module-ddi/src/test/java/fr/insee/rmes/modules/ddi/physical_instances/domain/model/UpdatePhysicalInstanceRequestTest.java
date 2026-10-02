package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UpdatePhysicalInstanceRequestTest {

    @Test
    void attachesToStudyUnit_whenTheStudyUnitIsFullyIdentified() {
        assertThat(new UpdatePhysicalInstanceRequest("PI", "DR", "LR", "su-1", "fr.insee", null, null)
                        .attachesToStudyUnit())
                .isTrue();
    }

    @Test
    void doesNotAttachToStudyUnit_withoutStudyUnit() {
        assertThat(new UpdatePhysicalInstanceRequest("PI", "DR", "LR").attachesToStudyUnit())
                .isFalse();
    }

    @Test
    void doesNotAttachToStudyUnit_withoutStudyUnitAgency() {
        assertThat(new UpdatePhysicalInstanceRequest("PI", "DR", "LR", "su-1", null, null, null).attachesToStudyUnit())
                .isFalse();
    }
}
