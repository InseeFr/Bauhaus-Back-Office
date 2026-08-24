package fr.insee.rmes.bauhaus_services.utils;

import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationService;
import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import fr.insee.rmes.json.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OrganisationLookup {

    private final OrganizationsService organizationsService;
    private final OrganisationsRepository organisationsRepository;
    private final OrganisationService organisationService;

    public OrganisationLookup(OrganizationsService organizationsService,
                              OrganisationsRepository organisationsRepository,
                              OrganisationService organisationService) {
        this.organizationsService = organizationsService;
        this.organisationsRepository = organisationsRepository;
        this.organisationService = organisationService;
    }

    public Optional<String> resolve(String value) throws RmesException {
        if (value == null) {
            return Optional.empty();
        }
        if (isIri(value)) {
            return Optional.of(value);
        }
        return Optional.ofNullable(organizationsService.getOrganizationUriById(value));
    }

    public List<String> findUnknown(List<String> values) throws RmesException {
        List<String> unknown = new ArrayList<>();
        if (values == null) {
            return unknown;
        }
        for (String value : values) {
            if (value == null) {
                continue;
            }
            if (isIri(value)) {
                if (!existsInGraph(value)) {
                    unknown.add(value);
                }
            } else if (organizationsService.getOrganizationUriById(value) == null) {
                unknown.add(value);
            }
        }
        return unknown;
    }

    public JSONArray canonicalize(JSONArray rows) throws RmesException {
        JSONArray result = new JSONArray();
        if (rows == null || rows.isEmpty()) {
            return result;
        }
        List<String> values = JSONUtils.streamValues(rows)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
        if (values.isEmpty()) {
            return result;
        }
        Map<String, OrganisationOption> organisationsMap = organisationService.getOrganisationsMap(values);
        if (organisationsMap == null) {
            return result;
        }
        for (String value : values) {
            OrganisationOption option = organisationsMap.get(value);
            if (option != null && option.stamp() != null && !option.stamp().isBlank()) {
                result.put(option.stamp());
            }
        }
        return result;
    }

    private boolean existsInGraph(String iri) throws RmesException {
        try {
            return organisationsRepository.checkIfOrganisationExists(iri);
        } catch (OrganisationFetchException e) {
            throw new RmesException(500, "Failed to check organisation existence", e.getMessage());
        }
    }

    private static boolean isIri(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }
}
