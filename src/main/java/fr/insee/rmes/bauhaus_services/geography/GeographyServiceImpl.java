package fr.insee.rmes.bauhaus_services.geography;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.geography.GeoFeature;
import fr.insee.rmes.persistance.ontologies.GEO;
import fr.insee.rmes.persistance.ontologies.IGEO;
import fr.insee.rmes.persistance.sparql_queries.geography.GeoQueries;

@Service
public class GeographyServiceImpl extends RdfService implements GeographyService {
	
	private static final String CAN_T_READ_REQUEST_BODY = "Can't read request body";


	private static final String HAS_COMPOSITION = "hasComposition";
	static final Logger logger = LogManager.getLogger(GeographyServiceImpl.class);

	@Override
	public String getGeoFeatures() throws RmesException {
		logger.info("Starting to get geo features");
		JSONArray resQuery = repoGestion.getResponseAsArray(GeoQueries.getFeaturesQuery());
		if (resQuery.length() != 0) {
			for (int i = resQuery.length() - 1; i >= 0; i--) {
				JSONObject feature = resQuery.getJSONObject(i);
				if (feature.has(HAS_COMPOSITION)) {
					if (feature.getBoolean(HAS_COMPOSITION)) {
						addUnionsAndDifferenceToJsonObject(feature);
					}
					feature.remove(HAS_COMPOSITION);
				}
			}
		}
		logger.info("Get geo features is done");
		return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
	}

	@Override
	public JSONObject getGeoFeature(IRI uri) throws RmesException {
		logger.info("Starting to get geo feature");

		JSONObject feature = repoGestion.getResponseAsObject(GeoQueries.getFeatureQuery(uri.stringValue()));
		if (feature.has(HAS_COMPOSITION)) {
			if (feature.getBoolean(HAS_COMPOSITION)) {
				addUnionsAndDifferenceToJsonObject(feature);
			}
			feature.remove(HAS_COMPOSITION);
		}
		return feature;
	}
	
	@Override
	public JSONObject getGeoFeatureById(String id) throws RmesException {
		return getGeoFeature(getGeoUriFromId(id));
	}

	/**
	 * Get in COG and in geo statistical territories (for sims)
	 * @param id
	 * @return
	 * @throws RmesException
	 */
	private IRI getGeoUriFromId(String id) throws RmesException {
		IRI uri ;
		JSONObject uriInCog = repoGestion.getResponseAsObject(GeoQueries.getGeoUriIfExists(id));
		if (uriInCog.has(Constants.URI) && StringUtils.isNotBlank(uriInCog.get(Constants.URI).toString())) {
			uri = RdfUtils.createIRI(uriInCog.get(Constants.URI).toString());
		}else {
			uri = RdfUtils.objectIRI(ObjectType.GEO_STAT_TERRITORY, id);
		}
		return uri;
	}

	private void addUnionsAndDifferenceToJsonObject(JSONObject feature) throws RmesException {
		String uriFeature = feature.getString(Constants.URI);
		JSONArray unions = repoGestion.getResponseAsArray(GeoQueries.getUnionsForAFeatureQuery(uriFeature));
		feature.put("unions", unions);
		JSONArray diff = repoGestion.getResponseAsArray(GeoQueries.getDifferenceForAFeatureQuery(uriFeature));
		feature.put("difference", diff);
	}

	@Override
	public String createFeature(String body) throws RmesException {
		if(!stampsRestrictionsService.canManageDocumentsAndLinks()) {
			throw new RmesUnauthorizedException(ErrorCodes.DOCUMENT_CREATION_RIGHTS_DENIED, "Only an admin or a manager can create a new geo feature.");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		UUID id = UUID.randomUUID();
		GeoFeature geoFeature = new GeoFeature();
		try {
			geoFeature = mapper.readValue(body,GeoFeature.class);
			geoFeature.setId(id.toString());
			if (geoFeature.getCode()==null) {
				geoFeature.setCode(id.toString());
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		createRdfGeoFeature(geoFeature);
		logger.info("Create geofeature : {} - {}", id , geoFeature.getLabelLg1());
		return id.toString();
	}
	
	public void createRdfGeoFeature(GeoFeature geoFeature) throws RmesException {
		Model model = new LinkedHashModel();
		if (geoFeature == null || StringUtils.isEmpty(geoFeature.getId())) {
			throw new RmesNotFoundException(ErrorCodes.GEOFEATURE_UNKNOWN, "No uri found", CAN_T_READ_REQUEST_BODY);
		}
		if (StringUtils.isEmpty(geoFeature.getLabelLg1())) {
			throw new RmesNotFoundException(ErrorCodes.GEOFEATURE_INCORRECT_BODY, "LabelLg1 not found", CAN_T_READ_REQUEST_BODY);
		}
		IRI geoIRI = RdfUtils.objectIRI(ObjectType.GEO_STAT_TERRITORY, geoFeature.getId());
		/*Const*/
		model.add(geoIRI, RDF.TYPE, IGEO.TERRITOIRE_STATISTIQUE, RdfUtils.simsGeographyGraph());
		/*Required*/
		model.add(geoIRI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(geoFeature.getLabelLg1(), Config.LG1), RdfUtils.simsGeographyGraph());

		/*Optional*/
		RdfUtils.addTripleString(geoIRI, IGEO.NOM, geoFeature.getLabelLg2(), Config.LG2, model, RdfUtils.simsGeographyGraph());
		RdfUtils.addTripleStringMdToXhtml(geoIRI, DCTERMS.ABSTRACT, geoFeature.getDescriptionLg1(), Config.LG1, model, RdfUtils.simsGeographyGraph());
		RdfUtils.addTripleStringMdToXhtml(geoIRI, DCTERMS.ABSTRACT, geoFeature.getDescriptionLg2(), Config.LG2, model, RdfUtils.simsGeographyGraph());


		geoFeature.getUnions().forEach(feature -> {
			RdfUtils.addTripleUri(geoIRI, GEO.UNION, feature.getUri(), model, RdfUtils.simsGeographyGraph());

		});
		geoFeature.getDifference().forEach(feature -> {
			RdfUtils.addTripleUri(geoIRI, GEO.DIFFERENCE, feature.getUri(), model, RdfUtils.simsGeographyGraph());
		});
		repoGestion.loadSimpleObject(geoIRI, model);
	}

}
