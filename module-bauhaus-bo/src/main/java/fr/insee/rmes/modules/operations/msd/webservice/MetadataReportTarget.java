package fr.insee.rmes.modules.operations.msd.webservice;

import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import org.apache.commons.lang3.StringUtils;

/**
 * Module de la cible d'un rapport, dans l'ordre de priorité de {@link Documentation#getIdTarget()} :
 * c'est d'elle que le rapport hérite ses propriétaires, faute de {@code dc:creator} propre.
 */
public final class MetadataReportTarget {

    private MetadataReportTarget() {}

    /** Le module de la cible postée, ou {@code null} si le rapport n'en a pas. */
    public static String parentModule(Documentation sims) {
        if (StringUtils.isNotEmpty(sims.getIdOperation())) {
            return RBAC.Module.OPERATION_OPERATION.name();
        }
        if (StringUtils.isNotEmpty(sims.getIdSeries())) {
            return RBAC.Module.OPERATION_SERIES.name();
        }
        if (StringUtils.isNotEmpty(sims.getIdIndicator())) {
            return RBAC.Module.OPERATION_INDICATOR.name();
        }
        return null;
    }
}
