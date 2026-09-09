package fr.insee.rmes.modules.organisations;

import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Vérifie au démarrage que {@code fr.insee.rmes.bauhaus.defaultContributor} désigne bien
 * une organisation de la base de gestion.
 * <p>
 * Cette valeur est servie au front par {@code /api/init} et sert de contributeur par défaut à la
 * création d'un concept : une IRI erronée ne se voit qu'au moment où un utilisateur enregistre un
 * concept mal attribué. On préfère faire échouer le démarrage.
 * <p>
 * Désactivable avec {@code fr.insee.rmes.bauhaus.default-contributor-check-enabled=false}
 * (déjà le cas dans les contextes de test, qui n'ont pas de base de gestion).
 */
@Component
@ConditionalOnProperty(
        name = "fr.insee.rmes.bauhaus.default-contributor-check-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DefaultContributorValidator {

    private static final String PROPERTY_NAME = "fr.insee.rmes.bauhaus.defaultContributor";

    private static final Logger logger = LoggerFactory.getLogger(DefaultContributorValidator.class);

    private final String defaultContributor;
    private final OrganisationsRepository organisationsRepository;

    public DefaultContributorValidator(
            @Value("${" + PROPERTY_NAME + ":}") String defaultContributor,
            OrganisationsRepository organisationsRepository) {
        this.defaultContributor = defaultContributor;
        this.organisationsRepository = organisationsRepository;
    }

    @PostConstruct
    public void validate() {
        String iri = requireAbsoluteUri();
        if (!existsInManagementDatabase(iri)) {
            throw new InvalidDefaultContributorException(PROPERTY_NAME + " = " + iri
                    + " : this organisation does not exist in the management database.");
        }
        logger.info("{} = {} : organisation trouvée en base de gestion", PROPERTY_NAME, iri);
    }

    private String requireAbsoluteUri() {
        if (defaultContributor == null || defaultContributor.isBlank()) {
            throw new InvalidDefaultContributorException(PROPERTY_NAME + " est vide : renseigner l'IRI "
                    + "de l'organisation contributrice par défaut.");
        }
        String iri = defaultContributor.trim();
        try {
            if (!new URI(iri).isAbsolute()) {
                throw new InvalidDefaultContributorException(invalidUriMessage(iri));
            }
        } catch (URISyntaxException e) {
            throw new InvalidDefaultContributorException(invalidUriMessage(iri), e);
        }
        return iri;
    }

    private static String invalidUriMessage(String iri) {
        return PROPERTY_NAME + " = " + iri + " : ce n'est pas une URI absolue "
                + "(attendu par exemple http://bauhaus/organisations/insee/HIE3014990).";
    }

    private boolean existsInManagementDatabase(String iri) {
        try {
            return organisationsRepository.checkIfOrganisationExists(iri);
        } catch (OrganisationFetchException e) {
            throw new InvalidDefaultContributorException(PROPERTY_NAME + " = " + iri
                    + " : n'a pas pu être vérifié en base de gestion.", e);
        }
    }
}
