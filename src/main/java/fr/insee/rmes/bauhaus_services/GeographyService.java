package fr.insee.rmes.bauhaus_services;

import org.eclipse.rdf4j.model.IRI;
import org.json.JSONObject;

import fr.insee.rmes.exceptions.RmesException;

public interface GeographyService {

	/******************************************************************************************
	 * COG
	 * *******************************************************************************************/

	String getGeoFeatures() throws RmesException;

	JSONObject getGeoFeatureById(String id) throws RmesException;

	String createFeature(String body)  throws RmesException;

	JSONObject getGeoFeature(IRI uri) throws RmesException;


    void updateFeature(String id, String body) throws RmesException;
}
