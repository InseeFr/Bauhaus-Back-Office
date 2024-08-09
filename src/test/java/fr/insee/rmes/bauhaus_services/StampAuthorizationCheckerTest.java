package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.model.rbac.RBAC;
import fr.insee.rmes.stubs.StampAuthorizationCheckerStub;
import org.junit.jupiter.api.Test;

import static fr.insee.rmes.stubs.StampAuthorizationCheckerStub.DATASET_STUB_ID;
import static org.assertj.core.api.Assertions.assertThat;

class StampAuthorizationCheckerTest {

    private final StampAuthorizationChecker stampAuthorizationChecker=new StampAuthorizationCheckerStub();

    @Test
    void userStampIsAuthorizedForResource() {

        //Given
        RBAC.Module module = RBAC.Module.DATASET;
        String id = DATASET_STUB_ID;
        //When then
        assertThat(this.stampAuthorizationChecker.userStampIsAuthorizedForResource(module, id)).isTrue();
    }
}