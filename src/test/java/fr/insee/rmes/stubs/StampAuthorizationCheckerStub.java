package fr.insee.rmes.stubs;

import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.auth.user.Stamp;

public class StampAuthorizationCheckerStub extends StampAuthorizationChecker {

    public static final String DATASET_STUB_ID = "1";

    public StampAuthorizationCheckerStub() {
        super(null, null, null, null);
    }

    @Override
    public boolean isDatasetManagerWithStamp(String datasetId, Stamp stamp) {
        return DATASET_STUB_ID.equals(datasetId);
    }
}
