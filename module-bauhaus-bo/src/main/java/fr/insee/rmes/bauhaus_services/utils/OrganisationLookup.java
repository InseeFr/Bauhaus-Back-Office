package fr.insee.rmes.bauhaus_services.utils;

import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OrganisationLookup {

    private final OrganizationsService organizationsService;
    private final OrganisationsRepository organisationsRepository;

    public OrganisationLookup(OrganizationsService organizationsService, OrganisationsRepository organisationsRepository) {
        this.organizationsService = organizationsService;
        this.organisationsRepository = organisationsRepository;
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
        if (rows == null) {
            return result;
        }
        for (int i = 0; i < rows.length(); i++) {
            Object raw = rows.get(i);
            if (raw == null) {
                continue;
            }
            Optional<String> resolved = resolve(raw.toString());
            resolved.ifPresent(result::put);
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
