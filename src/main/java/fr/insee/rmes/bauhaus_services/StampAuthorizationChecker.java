package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.accesscontrol.StampsRestrictionsVerifier;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.external.services.rbac.StampChecker;
import fr.insee.rmes.model.rbac.RBAC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StampAuthorizationChecker implements StampChecker {

    private final StampsRestrictionsVerifier stampsRestrictionsVerifier;

    @Autowired
    public StampAuthorizationChecker(StampsRestrictionsVerifier stampsRestrictionsVerifier) {
        this.stampsRestrictionsVerifier = stampsRestrictionsVerifier;
    }

    public boolean isSeriesManagerWithStamp(String seriesId, Stamp stamp) {
        return stampsRestrictionsVerifier.isSeriesManagerWithStamp(seriesId, stamp);
    }

    public boolean isDatasetManagerWithStamp(String datasetId, Stamp stamp) {
        return stampsRestrictionsVerifier.isDatasetManagerWithStamp(datasetId, stamp);
    }

    public boolean isDistributionManagerWithStamp(String datasetId, Stamp stamp) {
        return stampsRestrictionsVerifier.isDistributionManagerWithStamp(datasetId, stamp);
    }

    public boolean isCodesListManagerWithStamp(String codesListId, Stamp stamp) {
        return stampsRestrictionsVerifier.isCodesListManagerWithStamp(codesListId, stamp);
    }

    public boolean isStructureManagerWithStamp(String structureId, Stamp stamp) {
       return  stampsRestrictionsVerifier.isStructureManagerWithStamp(structureId, stamp);
    }

    public boolean isComponentManagerWithStamp(String componentId, Stamp stamp) {
        return stampsRestrictionsVerifier.isComponentManagerWithStamp(componentId,stamp);
    }

    @Override
    public boolean userStampIsAuthorizedForResource(RBAC.Module module, String id, Stamp stamp) {
        return switch (module){
            case CONCEPT -> false;
            case COLLECTION -> false;
            case FAMILY -> false;
            case SERIE -> false;
            case OPERATION -> false;
            case INDICATOR -> false;
            case SIMS -> false;
            case CLASSIFICATION -> false;
            case DATASET -> isDatasetManagerWithStamp(id, stamp);
            case null -> false;
        };
    }


}

