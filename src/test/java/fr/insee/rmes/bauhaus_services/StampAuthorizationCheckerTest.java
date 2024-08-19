package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.accesscontrol.AuthorizationCheckerWithResourceOwnershipByStamp;
import fr.insee.rmes.model.rbac.Module;
import fr.insee.rmes.stubs.StampRestritionVerifierStub;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;

import static fr.insee.rmes.stubs.StampRestritionVerifierStub.*;
import static org.assertj.core.api.Assertions.assertThat;

class StampAuthorizationCheckerTest {

    private final AuthorizationCheckerWithResourceOwnershipByStamp stampAuthorizationChecker=new AuthorizationCheckerWithResourceOwnershipByStamp(new StampRestritionVerifierStub());

    @ParameterizedTest
    @CsvSource({
            "'"+SERIES_STUB_ID+"', SERIE",
            "'"+DATASET_STUB_ID+"', DATASET",
            "'"+CODES_LISTES_STUB_ID+"', CODE_LIST"
    }
    )
    void userStampIsAuthorizedForResource(String id, Module module) {
        //When then
        assertThat(this.stampAuthorizationChecker.userStampIsAuthorizedForResource(module, id, null)).isTrue();
    }
}