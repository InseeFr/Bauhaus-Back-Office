package fr.insee.rmes.bauhaus_services.utils;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Resolves organisation stamps or IRIs into human-readable labels for exports.
 */
public final class OrganisationLabelResolver {

    private static final Logger logger = LoggerFactory.getLogger(OrganisationLabelResolver.class);

    private OrganisationLabelResolver() {
    }

    public static Map<String, OrganisationOption> organisationsByIdentifier(OrganisationService organisationService, Collection<String> rawIdentifiers) {
        List<String> identifiers = rawIdentifiers.stream()
                .filter(identifier -> identifier != null && !identifier.isBlank())
                .distinct()
                .toList();
        if (identifiers.isEmpty()) {
            return Map.of();
        }
        try {
            return organisationService.getOrganisationsMap(identifiers);
        } catch (RmesException e) {
            logger.warn("Unable to resolve organisation labels for {}, keeping raw stamps", identifiers, e);
            return Map.of();
        }
    }

    public static String labelOrReadableIdentifier(String identifier, Map<String, OrganisationOption> organisations) {
        if (identifier == null || identifier.isBlank()) {
            return identifier;
        }
        OrganisationOption organisation = organisations.get(identifier);
        if (organisation != null && organisation.label() != null && !organisation.label().isEmpty()) {
            return organisation.label();
        }
        return shortenIfIri(identifier);
    }

    private static String shortenIfIri(String identifier) {
        if (!identifier.startsWith("http://") && !identifier.startsWith("https://")) {
            return identifier;
        }
        int lastSlash = identifier.lastIndexOf('/');
        return lastSlash >= 0 && lastSlash < identifier.length() - 1
                ? identifier.substring(lastSlash + 1)
                : identifier;
    }
}
