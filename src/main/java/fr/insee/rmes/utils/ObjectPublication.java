package fr.insee.rmes.utils;

import fr.insee.rmes.exceptions.RmesException;

/**
 * Interface that will expose an utility method to validate if an object can be published,
 * and another one for publishing it if the validate methode does not throw an exception.
 * @param <T> The Java class related to the object we want to publish
 */
public interface ObjectPublication<T> {
    void validate(T object) throws RmesException;
    void publish(String id) throws RmesException;
}
