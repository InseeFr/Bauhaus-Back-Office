package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Lecture d'un item DDI par son identifiant, sans passer par le catalogue.
 * <p>
 * Les objets dont l'identifiant est dérivé de l'IRI de la ressource RDF qu'ils reflètent sont
 * adressables directement : inutile de balayer tout le dépôt pour les retrouver.
 */
class ColecticaItemByIdReader {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaItemByIdReader.class);

    private final ColecticaClient colecticaClient;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;

    ColecticaItemByIdReader(ColecticaClient colecticaClient, DDI3toDDI4ConverterService ddi3ToDdi4Converter) {
        this.colecticaClient = colecticaClient;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
    }

    /**
     * @return la StudyUnit d'identifiant {@code id}, ou {@link Optional#empty()} si Colectica ne la
     *     connaît pas. Seul le 404 vaut « absente » : une autre défaillance remonte, un dépôt
     *     injoignable ne devant pas passer pour une StudyUnit encore à créer.
     */
    Optional<Ddi4StudyUnit> findStudyUnit(String agencyId, String id) {
        ColecticaItemResponse item;
        try {
            item = colecticaClient.getItem(agencyId, id, null);
        } catch (HttpClientErrorException.NotFound _) {
            logger.info("No study unit {}/{} in Colectica yet", agencyId, id);
            return Optional.empty();
        }
        if (item == null || item.item() == null || item.item().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(ddi3ToDdi4Converter.toStudyUnit(ColecticaXml.stripLeadingGarbage(item.item())));
    }
}
