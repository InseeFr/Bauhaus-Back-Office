package fr.insee.rmes.webservice;

import fr.insee.rmes.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.checks.CheckResult;
import fr.insee.rmes.domain.port.serverside.RuleChecker;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ConceptDateChecker implements RuleChecker  {
    private final RepositoryGestion repositoryGestion;
    private final Config config;
    private static final Logger logger = LoggerFactory.getLogger(ConceptDateChecker.class);

    public ConceptDateChecker(RepositoryGestion repositoryGestion, Config config) {
        this.repositoryGestion = repositoryGestion;
        this.config = config;
    }

    @Override
    public Optional<CheckResult> check() {
        JSONArray concepts = null;
        Map<String, Object> result = new HashMap<>();
        try {
            concepts = this.getConceptsWithDates();
            List<Map<String, Object>> invalidConcepts = new ArrayList<>();
            int totalConcepts = concepts.length();
            int validConcepts = 0;

            for (int i = 0; i < concepts.length(); i++) {
                JSONObject concept = concepts.getJSONObject(i);
                String conceptId = concept.optString("id", "");
                String created = concept.optString("created", "");
                String modified = concept.optString("modified", "");

                boolean createdValid = validateISO8601Date(created);
                boolean modifiedValid = modified.isEmpty() || validateISO8601Date(modified);

                if (!createdValid || !modifiedValid) {
                    Map<String, Object> invalidConcept = new HashMap<>();
                    invalidConcept.put("conceptId", conceptId);
                    invalidConcept.put("created", created);
                    invalidConcept.put("createdValid", createdValid);
                    if (!modified.isEmpty()) {
                        invalidConcept.put("modified", modified);
                        invalidConcept.put("modifiedValid", modifiedValid);
                    }
                    invalidConcepts.add(invalidConcept);
                } else {
                    validConcepts++;
                }
            }


            result.put("status", "completed");
            result.put("description", "Concepts date format validation (ISO8601)");
            result.put("totalConcepts", totalConcepts);
            result.put("validConcepts", validConcepts);
            result.put("invalidConcepts", invalidConcepts.size());
            result.put("invalidConceptsList", invalidConcepts);
        } catch (RmesException e) {
            result.put("status", "error");
        }

        return Optional.of(new CheckResult("ConceptDateChecker", result));

    }

    public JSONArray getConceptsWithDates() throws RmesException {
        logger.info("Executing SPARQL query to get concepts with their created and modified dates");
        
        String query = getSparqlQuery();
        logger.debug("Executing query: {}", query);
        
        JSONArray results = repositoryGestion.getResponseAsArray(query);
        logger.info("Retrieved {} concepts from database", results.length());
        
        return results;
    }
    
    public String getSparqlQuery() throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("CONCEPTS_GRAPH", this.config.getConceptsGraph());
        return FreeMarkerUtils.buildRequest("checks/", "checkConceptsDateFormat.ftlh", params);

    }

    private boolean validateISO8601Date(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return false;
        }
        try {
            Instant.parse(dateString.trim()).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            return true;
        } catch (DateTimeParseException e){
            return false;
        }
    }

}