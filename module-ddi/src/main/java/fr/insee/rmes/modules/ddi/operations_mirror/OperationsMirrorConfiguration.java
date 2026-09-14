package fr.insee.rmes.modules.ddi.operations_mirror;

import fr.insee.rmes.modules.ddi.operations_mirror.domain.port.clientside.OperationsMirrorService;
import fr.insee.rmes.modules.ddi.operations_mirror.domain.services.DomainOperationsMirrorService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Branche le miroir DDI des séries et opérations statistiques, sous le drapeau
 * {@value #ENABLED_PROPERTY} (absent ou {@code false} : rien n'est branché).
 * <p>
 * Le drapeau ne porte que sur ce côté-ci : le domaine « opérations » publie ses événements dans tous
 * les cas, et sans écouteur ils n'ont ni effet ni coût. C'est ce qui permet d'ouvrir ou de refermer
 * la synchronisation sans toucher au chemin d'écriture RDF.
 * <p>
 * L'assemblage vit dans le package racine du module : le service de domaine ne peut pas dépendre de
 * la configuration Colectica, dont il ne reçoit ici que l'agence et les langues.
 */
@Configuration
@ConditionalOnProperty(name = OperationsMirrorConfiguration.ENABLED_PROPERTY, havingValue = "true")
public class OperationsMirrorConfiguration {

    public static final String ENABLED_PROPERTY = "fr.insee.rmes.bauhaus.colectica.operations-mirror.enabled";

    @Bean
    OperationsMirrorService operationsMirrorService(
            GroupService groupService,
            StudyUnitService studyUnitService,
            DDIService ddiService,
            ColecticaConfiguration colecticaConfiguration) {
        return new DomainOperationsMirrorService(
                groupService,
                studyUnitService,
                ddiService,
                colecticaConfiguration.server().defaultAgencyId(),
                colecticaConfiguration.langs());
    }

    @Bean
    OperationsMirrorEventListener operationsMirrorEventListener(OperationsMirrorService operationsMirrorService) {
        return new OperationsMirrorEventListener(operationsMirrorService);
    }
}
