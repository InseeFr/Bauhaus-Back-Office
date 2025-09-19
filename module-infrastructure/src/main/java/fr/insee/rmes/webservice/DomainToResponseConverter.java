package fr.insee.rmes.webservice;

/**
 * Generic interface for converting domain objects to response objects.
 * 
 * @param <D> the type of the domain object
 * @param <R> the type of the response object
 */
public interface DomainToResponseConverter<D, R> {
    
    /**
     * Converts a domain object to a response object.
     * 
     * @param domainObject the domain object to convert
     * @return the corresponding response object
     */
    static <D, R> R fromDomain(D domainObject) {
        throw new UnsupportedOperationException("The fromDomain method must be implemented by the concrete class");
    }
}