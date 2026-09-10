package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.concepts.concepts.LegacyConceptsRepository;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsSaveException;
import fr.insee.rmes.modules.concepts.concept.domain.model.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptDashboardItem;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptToValidate;
import fr.insee.rmes.modules.concepts.concept.domain.model.PartialConcept;
import fr.insee.rmes.modules.concepts.concept.domain.port.serverside.ConceptsRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptCollectionsQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.Deserializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@ServerSideAdaptor
@Repository
public class GraphDBConceptsRepository implements ConceptsRepository {

    private final RepositoryGestion repositoryGestion;
    private final ConceptCollectionsQueries conceptCollectionsQueries;
    private final ConceptConceptsQueries conceptConceptsQueries;
    private final BauhausLanguagesProperties languages;
    private final LegacyConceptsRepository legacyConceptsRepository;

    public GraphDBConceptsRepository(
            RepositoryGestion repositoryGestion,
            ConceptCollectionsQueries conceptCollectionsQueries,
            ConceptConceptsQueries conceptConceptsQueries,
            BauhausLanguagesProperties languages,
            @Lazy LegacyConceptsRepository legacyConceptsRepository) {
        this.repositoryGestion = repositoryGestion;
        this.conceptCollectionsQueries = conceptCollectionsQueries;
        this.conceptConceptsQueries = conceptConceptsQueries;
        this.languages = languages;
        this.legacyConceptsRepository = legacyConceptsRepository;
    }

    @Override
    public List<String> getCollectionIdsByConceptId(String conceptId) throws ConceptsFetchException {
        try {
            JSONArray results = repositoryGestion.getResponseAsArray(
                    conceptCollectionsQueries.getCollectionsByConceptId(conceptId));
            if (results == null) return List.of();
            return IntStream.range(0, results.length())
                    .mapToObj(i -> results.getJSONObject(i).getString("id"))
                    .toList();
        } catch (RmesException e) {
            throw new ConceptsFetchException(e);
        }
    }

    @Override
    public Optional<Concept> getConcept(ConceptId id) throws ConceptsFetchException {
        try {
            JSONObject row = repositoryGestion.getResponseAsObject(conceptConceptsQueries.conceptQuery(id.value()));
            if (row.isEmpty()) {
                return Optional.empty();
            }
            row.put("altLabelLg1", extractAltLabels(conceptConceptsQueries.altLabel(id.value(), languages.lg1())));
            row.put("altLabelLg2", extractAltLabels(conceptConceptsQueries.altLabel(id.value(), languages.lg2())));
            row.put("collectionIds", new JSONArray(getCollectionIdsByConceptId(id.value())));
            row.put("id", id.value());
            GraphDBConcept mapped = Deserializer.deserializeJSONObject(row, GraphDBConcept.class);
            return Optional.of(mapped.toDomain());
        } catch (RmesException e) {
            throw new ConceptsFetchException(e);
        }
    }

    private JSONArray extractAltLabels(String query) throws RmesException {
        JSONArray result = repositoryGestion.getResponseAsArray(query);
        if (result == null) return new JSONArray();
        JSONArray out = new JSONArray();
        JSONUtils.stream(result)
                .map(altLabel -> altLabel.optString("altLabel"))
                .filter(label -> !label.isEmpty())
                .forEach(out::put);
        return out;
    }

    @Override
    public List<PartialConcept> getConcepts() throws ConceptsFetchException {
        try {
            JSONArray rows = repositoryGestion.getResponseAsArray(conceptConceptsQueries.conceptsQuery());
            if (rows == null) return List.of();
            GraphDBPartialConcept[] mapped = Deserializer.deserializeJSONArray(rows, GraphDBPartialConcept[].class);
            Map<String, GraphDBPartialConcept> byId = new LinkedHashMap<>();
            for (GraphDBPartialConcept row : mapped) {
                byId.merge(row.id(), row, GraphDBPartialConcept::mergeAltLabelOf);
            }
            return byId.values().stream().map(GraphDBPartialConcept::toDomain).toList();
        } catch (RmesException e) {
            throw new ConceptsFetchException(e);
        }
    }

    @Override
    public List<ConceptToValidate> getConceptsToValidate() throws ConceptsFetchException {
        try {
            JSONArray rows = repositoryGestion.getResponseAsArray(conceptConceptsQueries.conceptsToValidateQuery());
            if (rows == null) return List.of();
            GraphDBConceptToValidate[] mapped =
                    Deserializer.deserializeJSONArray(rows, GraphDBConceptToValidate[].class);
            return Arrays.stream(mapped).map(GraphDBConceptToValidate::toDomain).toList();
        } catch (RmesException e) {
            throw new ConceptsFetchException(e);
        }
    }

    @Override
    public List<ConceptDashboardItem> getConceptsDashboard() {
        throw new UnsupportedOperationException(
                "Concept dashboard SPARQL is not defined yet; no caller depends on this method.");
    }

    @Override
    public ConceptId nextConceptId() throws ConceptsFetchException {
        try {
            JSONObject row = repositoryGestion.getResponseAsObject(conceptConceptsQueries.lastConceptID());
            if (row == null || row.isEmpty()) {
                return new ConceptId("c0001");
            }
            String notation = row.getString("notation");
            int next = Integer.parseInt(notation.substring(1)) + 1;
            return new ConceptId("c" + next);
        } catch (RmesException e) {
            throw new ConceptsFetchException(e);
        }
    }

    @Override
    public void save(Concept concept) throws ConceptsSaveException {
        delegateUpsert(concept);
    }

    @Override
    public void update(Concept concept) throws ConceptsSaveException {
        delegateUpsert(concept);
    }

    private void delegateUpsert(Concept concept) throws ConceptsSaveException {
        try {
            String body = toLegacyJsonBody(concept);
            legacyConceptsRepository.setConcept(concept.id().value(), body);
        } catch (RmesException e) {
            throw new ConceptsSaveException(e);
        }
    }

    private static String toLegacyJsonBody(Concept concept) {
        JSONObject body = new JSONObject();
        body.put("prefLabelLg1", concept.prefLabel().value());

        List<String> altLabelsLg1 = new ArrayList<>();
        List<String> altLabelsLg2 = new ArrayList<>();
        String prefLabelLg2 = null;
        for (var alt : concept.alternativeLabels()) {
            if (alt.lang() == Lang.alternativeLanguage()) {
                if (prefLabelLg2 == null) {
                    prefLabelLg2 = alt.value();
                } else {
                    altLabelsLg2.add(alt.value());
                }
            } else {
                altLabelsLg1.add(alt.value());
            }
        }
        if (prefLabelLg2 != null) body.put("prefLabelLg2", prefLabelLg2);
        if (!altLabelsLg1.isEmpty()) body.put("altLabelLg1", new JSONArray(altLabelsLg1));
        if (!altLabelsLg2.isEmpty()) body.put("altLabelLg2", new JSONArray(altLabelsLg2));

        body.put("creator", concept.creator());
        concept.contributor().ifPresent(c -> body.put("contributor", c));
        body.put("disseminationStatus", concept.disseminationStatus());
        if (!concept.collectionIds().isEmpty()) {
            body.put("collections", new JSONArray(concept.collectionIds()));
        }
        return body.toString();
    }

    @Override
    public Set<String> findExistingConceptIds(List<String> ids) throws ConceptsFetchException {
        if (ids.isEmpty()) return Set.of();
        try {
            JSONArray results =
                    repositoryGestion.getResponseAsArray(conceptConceptsQueries.findExistingConceptIds(ids));
            if (results == null) return Set.of();
            return IntStream.range(0, results.length())
                    .mapToObj(i -> results.getJSONObject(i).getString("id"))
                    .collect(Collectors.toSet());
        } catch (RmesException e) {
            throw new ConceptsFetchException(e);
        }
    }

    @Override
    public Set<String> findValidatedConceptIds(List<String> ids) throws ConceptsFetchException {
        if (ids.isEmpty()) return Set.of();
        try {
            JSONArray results =
                    repositoryGestion.getResponseAsArray(conceptConceptsQueries.findValidatedConceptIds(ids));
            if (results == null) return Set.of();
            return IntStream.range(0, results.length())
                    .mapToObj(i -> results.getJSONObject(i).getString("id"))
                    .collect(Collectors.toSet());
        } catch (RmesException e) {
            throw new ConceptsFetchException(e);
        }
    }

    @Override
    public void validate(List<ConceptId> ids) throws ConceptsSaveException {
        if (ids.isEmpty()) return;
        try {
            JSONArray body = new JSONArray();
            ids.forEach(id -> body.put(id.value()));
            legacyConceptsRepository.conceptsValidation(body.toString());
        } catch (RmesException e) {
            throw new ConceptsSaveException(e);
        }
    }

    @Override
    public void delete(ConceptId id) throws ConceptsSaveException {
        try {
            legacyConceptsRepository.deleteConcept(id.value());
        } catch (RmesException e) {
            throw new ConceptsSaveException(e);
        }
    }
}
