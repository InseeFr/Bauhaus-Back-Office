package fr.insee.rmes.modules.organisations.domain.model;

/**
 * Lightweight projection of an organisation used for listing purposes.
 * <p>
 * It carries the data consumed by the front-end organisations list: the IRI,
 * the {@code adms:identifier}, the preferred label in the main language and the
 * preferred label in the alternative language (falling back to the main one).
 * </p>
 */
public record OrganisationSummary(String iri, String identifier, String label, String labelLg2) {
}
