package fr.insee.rmes.bauhaus_services.geography;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.*;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.graphdb.ontologies.GEO;
import fr.insee.rmes.graphdb.ontologies.IGEO;
import fr.insee.rmes.modules.geographies.infrastructure.graphdb.GeographyQueries;
import fr.insee.rmes.modules.geographies.model.GeoFeature;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GeographyServiceImpl extends RdfService implements GeographyService {
    private final BauhausLanguagesProperties languages;

    public static final String GEO_FEATURES_CACHE = "geoFeatures";

    private static final String HAS_COMPOSITION = "hasComposition";
    private static final String UNIONS = "unions";
    private static final String DIFFERENCE = "difference";
    private static final String FEATURE = "feature";
    private static final String COMPOSITION = "composition";
    static final Logger logger = LoggerFactory.getLogger(GeographyServiceImpl.class);

    private final GeographyQueries geographyQueries;

    public GeographyServiceImpl(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            BauhausLanguagesProperties languages,
            PublicationUtils publicationUtils,
            GeographyQueries geographyQueries) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.languages = languages;
        this.geographyQueries = geographyQueries;
    }

    @Override
    @Cacheable(GEO_FEATURES_CACHE)
    public String getGeoFeatures() throws RmesException {
        logger.info("Starting to get geo features");
        JSONArray resQuery = repoGestion.getResponseAsArray(geographyQueries.getFeaturesQuery());
        if (!resQuery.isEmpty()) {
            Map<String, Map<String, JSONArray>> compositions = readAllCompositions();
            for (int i = 0; i < resQuery.length(); i++) {
                JSONObject feature = resQuery.getJSONObject(i);
                if (feature.has(HAS_COMPOSITION)) {
                    if (feature.getBoolean(HAS_COMPOSITION)) {
                        Map<String, JSONArray> composition =
                                compositions.getOrDefault(feature.getString(Constants.URI), Map.of());
                        feature.put(UNIONS, composition.getOrDefault(UNIONS, new JSONArray()));
                        feature.put(DIFFERENCE, composition.getOrDefault(DIFFERENCE, new JSONArray()));
                    }
                    feature.remove(HAS_COMPOSITION);
                }
            }
        }
        logger.info("Get geo features is done");
        return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
    }

    /**
     * Unions et différences de tous les territoires, rangées par URI de territoire puis par nature
     * ({@code unions} / {@code difference}) : une seule requête au lieu de deux par territoire.
     */
    private Map<String, Map<String, JSONArray>> readAllCompositions() throws RmesException {
        JSONArray rows = repoGestion.getResponseAsArray(geographyQueries.getAllUnionsAndDifferencesQuery());
        Map<String, Map<String, JSONArray>> compositions = new HashMap<>();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            String feature = (String) row.remove(FEATURE);
            String kind = (String) row.remove(COMPOSITION);
            compositions
                    .computeIfAbsent(feature, k -> new HashMap<>())
                    .computeIfAbsent(kind, k -> new JSONArray())
                    .put(row);
        }
        return compositions;
    }

    @Override
    public JSONObject getGeoFeature(IRI uri) throws RmesException {
        logger.info("Starting to get geo feature");

        JSONObject feature = repoGestion.getResponseAsObject(geographyQueries.getFeatureQuery(uri.stringValue()));
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
        IRI uri;
        JSONObject uriInCog = repoGestion.getResponseAsObject(geographyQueries.getGeoUriIfExists(id));
        if (uriInCog.has(Constants.URI)
                && StringUtils.isNotBlank(uriInCog.get(Constants.URI).toString())) {
            uri = RdfUtils.createIRI(uriInCog.get(Constants.URI).toString());
        } else {
            uri = RdfUtils.objectIRI(ObjectType.GEO_STAT_TERRITORY, id);
        }
        return uri;
    }

    private void addUnionsAndDifferenceToJsonObject(JSONObject feature) throws RmesException {
        String uriFeature = feature.getString(Constants.URI);
        JSONArray unions = repoGestion.getResponseAsArray(geographyQueries.getUnionsForAFeatureQuery(uriFeature));
        feature.put(UNIONS, unions);
        JSONArray diff = repoGestion.getResponseAsArray(geographyQueries.getDifferenceForAFeatureQuery(uriFeature));
        feature.put(DIFFERENCE, diff);
    }

    @Override
    @CacheEvict(cacheNames = GEO_FEATURES_CACHE, allEntries = true)
    public void updateFeature(String id, GeoFeature geoFeature) throws RmesException {

        // Est-ce il existe une geographie ayant l'id passé en paramètre
        // si c'est non => 404

        geoFeature.setId(id);

        if (geoFeature.getCode() == null) {
            geoFeature.setCode(geoFeature.getId());
        }
        createRdfGeoFeature(geoFeature);
    }

    @Override
    @CacheEvict(cacheNames = GEO_FEATURES_CACHE, allEntries = true)
    public String createFeature(GeoFeature geoFeature) throws RmesException {
        UUID id = UUID.randomUUID();
        geoFeature.setId(id.toString());
        if (geoFeature.getCode() == null) {
            geoFeature.setCode(id.toString());
        }
        String iri = createRdfGeoFeature(geoFeature);
        logger.info("Create geofeature : {} - {}", id, geoFeature.getLabelLg1());
        return iri;
    }

    public String createRdfGeoFeature(GeoFeature geoFeature) throws RmesException {

        if (StringUtils.isEmpty(geoFeature.getId())) {
            throw new RmesBadRequestException(ErrorCodes.GEOFEATURE_UNKNOWN, "id is mandatory");
        }

        if (StringUtils.isEmpty(geoFeature.getLabelLg1())) {
            throw new RmesBadRequestException(ErrorCodes.GEOFEATURE_INCORRECT_BODY, "LabelLg1 is mandatory");
        }

        if (StringUtils.isEmpty(geoFeature.getLabelLg2())) {
            throw new RmesBadRequestException(ErrorCodes.GEOFEATURE_INCORRECT_BODY, "LabelLg2 is mandatory");
        }

        IRI geoIRI = RdfUtils.objectIRI(ObjectType.GEO_STAT_TERRITORY, geoFeature.getId());

        // We check the unicity of the value
        JSONObject checkUnicityTerritory =
                repoGestion.getResponseAsObject(geographyQueries.checkUnicityTerritory(geoFeature.getLabelLg1()));
        if (checkUnicityTerritory.has("territory")
                && !checkUnicityTerritory.getString("territory").equalsIgnoreCase(geoFeature.getUri())) {
            throw new RmesBadRequestException(ErrorCodes.GEOFEATURE_EXISTING_LABEL, "The labelLg1 already exists");
        }

        Model model = new LinkedHashModel();

        /*Const*/
        model.add(geoIRI, RDF.TYPE, IGEO.TERRITOIRE_STATISTIQUE, RdfUtils.simsGeographyGraph());
        /*Required*/
        model.add(
                geoIRI,
                SKOS.PREF_LABEL,
                RdfUtils.setLiteralString(geoFeature.getLabelLg1(), languages.lg1()),
                RdfUtils.simsGeographyGraph());

        /*Optional*/
        RdfUtils.addTripleString(
                geoIRI,
                SKOS.PREF_LABEL,
                geoFeature.getLabelLg2(),
                languages.lg2(),
                model,
                RdfUtils.simsGeographyGraph());
        RdfUtils.addTripleString(
                geoIRI, IGEO.NOM, geoFeature.getLabelLg2(), languages.lg2(), model, RdfUtils.simsGeographyGraph());
        RdfUtils.addTripleStringMdToXhtml(
                geoIRI,
                DCTERMS.ABSTRACT,
                geoFeature.getDescriptionLg1(),
                languages.lg1(),
                model,
                RdfUtils.simsGeographyGraph());
        RdfUtils.addTripleStringMdToXhtml(
                geoIRI,
                DCTERMS.ABSTRACT,
                geoFeature.getDescriptionLg2(),
                languages.lg2(),
                model,
                RdfUtils.simsGeographyGraph());

        geoFeature
                .getUnions()
                .forEach(feature -> RdfUtils.addTripleUri(
                        geoIRI, GEO.UNION, feature.getUri(), model, RdfUtils.simsGeographyGraph()));
        geoFeature
                .getDifference()
                .forEach(feature -> RdfUtils.addTripleUri(
                        geoIRI, GEO.DIFFERENCE, feature.getUri(), model, RdfUtils.simsGeographyGraph()));
        repoGestion.loadSimpleObject(geoIRI, model);

        return RdfUtils.toString(geoIRI);
    }
}
