package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONObject;

public interface GeographyService {

	String getGeoFeatures() throws RmesException;

	JSONObject getGeoFeatureById(String id) throws RmesException;

	String createFeature(String body)  throws RmesException;

	JSONObject getGeoFeature(IRI uri) throws RmesException;

    void updateFeature(String id, String body) throws RmesException;
}
