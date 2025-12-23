package fr.insee.rmes.modules.organisations.domain.port.clientside;

import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.model.CompactOrganisation;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

/**
 * Client-side port for organization services in the organisations module.
 * <p>
 * This interface defines the contract for accessing compact organization data from the client perspective.
 * It provides methods to retrieve organization information by identifier or IRI, perform batch operations,
 * and check for organization existence.
 * </p>
 * <p>
 * This is a port in the hexagonal architecture pattern, allowing the domain layer to communicate
 * with external clients without depending on their implementation details. The service focuses on
 * compact organization representations optimized for lightweight data exchange.
 * </p>
 *
 * @see CompactOrganisation
 * @see OrganisationFetchException
 * @since 4.17.0
 */
public interface OrganisationsService {

    /**
     * Retrieves a compact organization by its unique identifier.
     * <p>
     * This method fetches a lightweight representation of an organization containing
     * essential information such as IRI, identifier, and localized label.
     * </p>
     *
     * @param id The unique identifier of the organization to retrieve. Must not be null or empty.
     * @return A {@link CompactOrganisation} object containing the organization's essential data
     * @throws OrganisationFetchException if the retrieval operation fails, if the identifier is invalid,
     *         or if no organization is found with the given identifier
     */
    CompactOrganisation getCompactOrganisation(String id) throws OrganisationFetchException;

    /**
     * Retrieves multiple compact organizations by their identifiers in a single batch operation.
     * <p>
     * This method is optimized for bulk retrieval, reducing the number of operations
     * compared to calling {@link #getCompactOrganisation(String)} multiple times.
     * It's particularly useful when you need to fetch several organizations at once.
     * </p>
     * <p>
     * The method returns all successfully retrieved organizations. The order of results
     * may not match the order of input identifiers.
     * </p>
     *
     * @param ids List of organization identifiers to retrieve. Must not be null.
     *            Can be empty, in which case an empty list is returned.
     * @return List of {@link CompactOrganisation} objects for all successfully found organizations.
     *         Organizations that were not found are not included in the result.
     * @throws OrganisationFetchException if the retrieval operation fails (e.g., database error,
     *         query execution error)
     */
    List<CompactOrganisation> getCompactOrganisations(List<String> ids) throws OrganisationFetchException;

    /**
     * Checks whether an organization exists in the system using its IRI.
     * <p>
     * This method performs a lightweight existence check without retrieving the full organization data.
     * It uses a SPARQL ASK query to verify if any triples exist for the given IRI.
     * </p>
     * <p>
     * This is more efficient than retrieving the organization and checking for null,
     * especially when you only need to verify existence.
     * </p>
     *
     * @param iri The IRI (Internationalized Resource Identifier) of the organization to check.
     *            Must be a valid IRI string (e.g., "http://bauhaus/organisations/insee/HIE2000052").
     *            Must not be null or empty.
     * @return {@code true} if an organization with the given IRI exists, {@code false} otherwise
     * @throws OrganisationFetchException if the existence check operation fails (e.g., database error,
     *         invalid IRI format, query execution error)
     */
    boolean checkIfOrganisationExists(String iri) throws OrganisationFetchException;

    Optional<String> getDctermsIdentifier(String admsIdentifier) throws OrganisationFetchException;

    Optional<String> getAdmsIdentifier(String dctermsIdentifier) throws OrganisationFetchException;
}
