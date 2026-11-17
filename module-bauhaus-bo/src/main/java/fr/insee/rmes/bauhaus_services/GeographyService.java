package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.geographies.model.GeoFeature;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONObject;

public interface GeographyService {

    String getGeoFeatures() throws RmesException;

    JSONObject getGeoFeatureById(String id) throws RmesException;

    String createFeature(GeoFeature geoFeature) throws RmesException;

    JSONObject getGeoFeature(IRI uri) throws RmesException;

    void updateFeature(String id, GeoFeature geoFeature) throws RmesException;
}
