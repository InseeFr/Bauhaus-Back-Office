package fr.insee.rmes.modules.organisations.webservice;

import fr.insee.rmes.modules.organisations.domain.model.OrganisationSummary;

/**
 * JSON representation of an organisation in the listing endpoint, matching the
 * shape consumed by the front-end ({@code iri}, {@code id}, {@code label},
 * {@code labelLg2}).
 */
public record OrganisationResponse(String iri, String id, String label, String labelLg2) {

    public static OrganisationResponse fromDomain(OrganisationSummary summary) {
        return new OrganisationResponse(summary.iri(), summary.identifier(), summary.label(), summary.labelLg2());
    }
}
