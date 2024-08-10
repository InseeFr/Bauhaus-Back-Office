package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.model.rbac.RBAC;

public interface StampChecker {
    boolean userStampIsAuthorizedForResource(RBAC.Module module, String id, Stamp stamp);

    @Deprecated
    boolean isSeriesManagerWithStamp(String s, Stamp stamp);

    @Deprecated
    boolean isCodesListManagerWithStamp(String s, Stamp stamp);

    @Deprecated
    boolean isDatasetManagerWithStamp(String s, Stamp stamp);

    @Deprecated
    boolean isDistributionManagerWithStamp(String s, Stamp stamp);

    @Deprecated
    boolean isStructureManagerWithStamp(String s, Stamp stamp);

    @Deprecated
    boolean isComponentManagerWithStamp(String s, Stamp stamp);
}
