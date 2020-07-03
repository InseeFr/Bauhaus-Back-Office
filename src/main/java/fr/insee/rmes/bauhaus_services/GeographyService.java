package fr.insee.rmes.bauhaus_services;

import org.json.JSONObject;

import fr.insee.rmes.exceptions.RmesException;

public interface GeographyService {

	/******************************************************************************************
	 * COG
	 * *******************************************************************************************/

	String getGeoFeatures() throws RmesException;

	JSONObject getGeoFeature(String id) throws RmesException;

	String createFeature(String body)  throws RmesException;



}
