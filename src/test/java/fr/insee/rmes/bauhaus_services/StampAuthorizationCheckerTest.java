package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.accesscontrol.AuthorizationCheckerWithResourceOwnershipByStamp;
import fr.insee.rmes.model.rbac.Module;
import fr.insee.rmes.stubs.StampRestritionVerifierStub;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static fr.insee.rmes.stubs.StampRestritionVerifierStub.*;
import static org.assertj.core.api.Assertions.assertThat;

class StampAuthorizationCheckerTest {

    private final AuthorizationCheckerWithResourceOwnershipByStamp stampAuthorizationChecker=new AuthorizationCheckerWithResourceOwnershipByStamp(new StampRestritionVerifierStub());
    @ParameterizedTest
    @ValueSource(strings = {SERIES_STUB_ID, DATASET_STUB_ID, DISTRIBUTION_STUB_ID, COMPONENT_STUB_ID, STRUCTURE_STUB_ID, CODES_LISTES_STUB_ID})
    void userStampIsAuthorizedForResource(String id) {
        //Given
        Module module = Module.DATASET;
        //When then
        assertThat(this.stampAuthorizationChecker.userStampIsAuthorizedForResource(module, id, null)).isTrue();
    }
}