package fr.insee.rmes.modules.organisations.domain.port.serverside;

import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.model.CompactOrganisation;

import java.util.List;
import java.util.Optional;

/**
 * Server-side port for compact organization data access.
 * <p>
 * This interface defines the contract for the persistence layer to provide compact organization data
 * to the domain layer. It abstracts the underlying data storage mechanism (GraphDB in this case)
 * allowing the domain layer to remain independent of infrastructure concerns.
 * </p>
 * <p>
 * This is a port in the hexagonal architecture pattern, serving as an adapter interface
 * for the domain to communicate with external data sources. It focuses on retrieving
 * compact organization representations optimized for performance and minimal data transfer.
 * </p>
 * <p>
 * Implementations of this interface are responsible for:
 * <ul>
 *   <li>Executing SPARQL queries against the RDF triple store</li>
 *   <li>Mapping query results from GraphDB format to {@link CompactOrganisation} domain objects</li>
 *   <li>Handling data access errors and converting them to {@link OrganisationFetchException}</li>
 *   <li>Optimizing queries for batch operations</li>
 * </ul>
 * </p>
 *
 * @see CompactOrganisation
 * @see OrganisationFetchException
 * @since 4.17.0
 */
public interface OrganisationsRepository {

    /**
     * Retrieves a compact organization by its unique identifier from the data source.
     * <p>
     * This method executes a SPARQL query to fetch an organization's essential data
     * (IRI, identifier, and localized label) from the RDF triple store and maps it
     * to a {@link CompactOrganisation} domain object.
     * </p>
     *
     * @param id The unique identifier of the organization to retrieve.
     *           Must not be null or empty.
     * @return A {@link CompactOrganisation} object containing the organization's data
     * @throws OrganisationFetchException if the retrieval operation fails (e.g., database connection error,
     *         query execution error, data mapping error, JSON deserialization error),
     *         if the identifier is invalid, or if no organization is found with the given identifier
     */
    CompactOrganisation getCompactOrganisation(String id) throws OrganisationFetchException;

    /**
     * Retrieves multiple compact organizations by their identifiers in a single batch operation.
     * <p>
     * This method is optimized for bulk retrieval, executing a single SPARQL query with a FILTER clause
     * to fetch multiple organizations at once, minimizing database round-trips compared to
     * calling {@link #getCompactOrganisation(String)} multiple times.
     * </p>
     * <p>
     * The implementation uses the {@code FILTER(?identifier IN (...))} SPARQL pattern to efficiently
     * retrieve all matching organizations in one query. Results are then mapped to
     * {@link CompactOrganisation} objects.
     * </p>
     * <p>
     * <strong>Important:</strong> The returned list only contains organizations that were successfully
     * retrieved from the data source. If an identifier doesn't correspond to any organization,
     * it will not be present in the result list. The order of results may not match the order
     * of input identifiers.
     * </p>
     *
     * @param ids List of organization identifiers to retrieve.
     *            Must not be null. Can be empty, in which case an empty list is returned.
     * @return List of {@link CompactOrganisation} objects for all successfully found organizations.
     *         Returns an empty list if no organizations are found or if the input list is empty.
     * @throws OrganisationFetchException if the retrieval operation fails (e.g., database connection error,
     *         query execution error, JSON array parsing error, data mapping error)
     */
    List<CompactOrganisation> getCompactOrganisations(List<String> ids) throws OrganisationFetchException;

    /**
     * Checks whether an organization exists in the system using its IRI.
     * <p>
     * This method performs a lightweight existence check by executing a SPARQL ASK query.
     * It verifies if any triples exist for the given IRI without retrieving the full organization data,
     * making it more efficient than fetching the organization and checking for null.
     * </p>
     * <p>
     * The implementation executes an ASK query of the form:
     * <pre>{@code
     * ASK {
     *     <IRI> ?p ?v .
     * }
     * }</pre>
     * This checks if there are any predicates and values associated with the given IRI.
     * </p>
     *
     * @param iri The IRI (Internationalized Resource Identifier) of the organization to check.
     *            Must be a valid IRI string (e.g., "http://bauhaus/organisations/insee/HIE2000052").
     *            Must not be null or empty.
     * @return {@code true} if an organization with the given IRI exists in the triple store,
     *         {@code false} otherwise
     * @throws OrganisationFetchException if the existence check operation fails (e.g., database connection error,
     *         invalid IRI format, query execution error, boolean response parsing error)
     */
    boolean checkIfOrganisationExists(String iri) throws OrganisationFetchException;

    Optional<String> getDctermsIdentifier(String admsIdentifier) throws OrganisationFetchException;

    Optional<String> getAdmsIdentifier(String dctermsIdentifier) throws OrganisationFetchException;
}
