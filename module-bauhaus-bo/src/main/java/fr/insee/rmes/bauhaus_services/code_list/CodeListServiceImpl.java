package fr.insee.rmes.bauhaus_services.code_list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.errors.CodesListErrorCodes;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.modules.codeslists.codeslists.webservice.CodeRequest;
import fr.insee.rmes.modules.codeslists.partialcodeslists.model.PartialCodesList;
import fr.insee.rmes.modules.commons.configuration.swagger.model.code_list.CodeList;
import fr.insee.rmes.modules.commons.configuration.swagger.model.code_list.Page;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.Deserializer;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.IdGenerator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class CodeListServiceImpl extends RdfService implements CodeListService {
    private final BauhausLanguagesProperties languages;

    private static final String LAST_CLASS_URI_SEGMENT = "lastClassUriSegment";

    private static final String CODE = "code";

    private static final String CODES = "codes";
    private static final String ITEMS = "items";

    private static final String LAST_LIST_URI_SEGMENT = "lastListUriSegment";

    private static final String LAST_CODE_URI_SEGMENT = "lastCodeUriSegment";

    public static final String VALIDATION_STATE = "validationState";
    public static final String CONCEPT = "concept/";

    private final CodeListPublication codeListPublication;

    private final CodeListsQueries codeListsQueries;

    public CodeListServiceImpl(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            BauhausLanguagesProperties languages,
            PublicationUtils publicationUtils,
            CodeListPublication codeListPublication,
            CodeListsQueries codeListsQueries) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.languages = languages;
        this.codeListPublication = codeListPublication;
        this.codeListsQueries = codeListsQueries;
    }

    @Override
    public String getCodesJson(String notation, int page, Integer perPage) throws RmesException {
        return getCodesAsJSONObject(notation, page, perPage).toString();
    }

    private JSONObject getCodesAsJSONObject(String notation, int page, Integer perPage) throws RmesException {
        JSONObject result = new JSONObject();

        JSONObject counter = repoGestion.getResponseAsObject(codeListsQueries.countCodesForCodeList(notation, null));

        result.put("total", counter.get("count"));
        result.put("page", page);
        result.put(ITEMS, getItemsWithPagination(notation, page, perPage));
        return result;
    }

    private JSONArray getItemsWithPagination(String notation, int page, Integer perPage) throws RmesException {
        return repoGestion.getResponseAsArray(codeListsQueries.getCodeListItemsByNotation(notation, page, perPage));
    }

    @Override
    public String getCodeListJson(String notation) throws RmesException {
        return getCodeListAsJSONObject(notation).toString();
    }

    private JSONObject getCodeListAsJSONObject(String notation) throws RmesException {
        JSONObject codeList = repoGestion.getResponseAsObject(codeListsQueries.getCodeListLabelByNotation(notation));
        if (codeList.isEmpty()) {
            throw new RmesNotFoundException(CodesListErrorCodes.CODE_LIST_UNKNOWN_ID, "CodeList not found", notation);
        }
        return codeList.put(Constants.NOTATION, notation);
    }

    @Override
    public CodeList getDetailedCodesList(String notation) throws RmesException {
        String detailedCodesList = getDetailedCodesListJson(notation).toString();
        return Deserializer.deserializeJsonString(detailedCodesList, CodeList.class);
    }

    @Override
    public String getDetailedPartialCodesList(String notation) throws RmesException {
        JSONObject detailedCodesList = getDetailedPartialCodesListJson(notation);
        return detailedCodesList.toString();
    }

    public JSONObject getDetailedCodesListJson(String notation) throws RmesException {
        JSONObject codeList = repoGestion.getResponseAsObject(codeListsQueries.getDetailedCodeListByNotation(notation));
        if (codeList.isEmpty()) {
            throw new RmesNotFoundException(CodesListErrorCodes.CODE_LIST_UNKNOWN_ID, "CodeList not found", notation);
        }
        this.repoGestion.getMultipleTripletsForObject(
                codeList,
                "contributor",
                codeListsQueries.getCodesListContributors(codeList.getString("iri")),
                "contributor");
        return codeList;
    }

    public JSONObject getDetailedPartialCodesListJson(String notation) throws RmesException {
        JSONObject detailedCodesList = getDetailedCodesListJson(notation);
        JSONArray codes = repoGestion.getResponseAsArray(
                codeListsQueries.getDetailedCodes(notation, CodeListKind.PARTIAL, null, 0, 0, null));
        formatCodesForPartialList(detailedCodesList, codes);
        return detailedCodesList;
    }

    /**
     * In order to avoid multiple loops, we group by the data by the code only once.
     */
    private JSONObject groupByBroaderNarrowerCloseMatchByCode(String notation) throws RmesException {
        JSONObject broaderNarrowerCloseMatchByCode = new JSONObject();

        JSONArray broaderNarrowerCloseMatch =
                repoGestion.getResponseAsArray(codeListsQueries.getBroaderNarrowerCloseMatch(notation));
        for (var i = 0; i < broaderNarrowerCloseMatch.length(); i++) {
            JSONObject broaderNarrowerCloseMatchCode = broaderNarrowerCloseMatch.getJSONObject(i);

            String mainCode = broaderNarrowerCloseMatchCode.getString("code");
            String linkCode = broaderNarrowerCloseMatchCode.getString("linkCode");
            String linkType = broaderNarrowerCloseMatchCode.getString("linkType");
            if (!broaderNarrowerCloseMatchByCode.has(mainCode)) {
                broaderNarrowerCloseMatchByCode.put(
                        mainCode,
                        new JSONObject()
                                .put("broader", new JSONArray())
                                .put("narrower", new JSONArray())
                                .put("closeMatch", new JSONArray()));
            }

            JSONObject codeToUpdate = broaderNarrowerCloseMatchByCode.getJSONObject(mainCode);
            codeToUpdate.getJSONArray(linkType).put(linkCode);
        }
        return broaderNarrowerCloseMatchByCode;
    }

    private void addLinkCodeToItem(JSONObject item, String key, JSONObject broaderNarrowerCloseMatchForCode) {
        if (!broaderNarrowerCloseMatchForCode.getJSONArray(key).isEmpty()) {
            item.put(key, broaderNarrowerCloseMatchForCode.getJSONArray(key));
        }
    }

    private void addBroaderNarrowerCloseMatchToItem(JSONObject item, JSONObject broaderNarrowerCloseMatchByCode) {
        if (broaderNarrowerCloseMatchByCode.has(item.getString("code"))) {
            JSONObject broaderNarrowerCloseMatchForCode =
                    broaderNarrowerCloseMatchByCode.getJSONObject(item.getString("code"));
            addLinkCodeToItem(item, "broader", broaderNarrowerCloseMatchForCode);
            addLinkCodeToItem(item, "narrower", broaderNarrowerCloseMatchForCode);
            addLinkCodeToItem(item, "closeMatch", broaderNarrowerCloseMatchForCode);
        }
    }

    @Override
    public Page getCodesForCodeList(String notation, List<String> search, int page, Integer perPage, String sort)
            throws RmesException {
        JSONObject result = new JSONObject();

        JSONObject counter = repoGestion.getResponseAsObject(codeListsQueries.countCodesForCodeList(notation, search));
        JSONArray items = repoGestion.getResponseAsArray(
                codeListsQueries.getDetailedCodes(notation, CodeListKind.FULL, search, page, perPage, sort));
        JSONObject broaderNarrowerCloseMatchByCode = groupByBroaderNarrowerCloseMatchByCode(notation);

        for (var i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            addBroaderNarrowerCloseMatchToItem(item, broaderNarrowerCloseMatchByCode);
        }

        result.put("total", counter.get("count"));
        result.put("page", page);
        result.put(ITEMS, items);
        return Deserializer.deserializeJsonString(String.valueOf(result), Page.class);
    }

    /**
     * Format the codes list for a partial code list.
     * We just need to remove the parents property.
     */
    private void formatCodesForPartialList(JSONObject codeList, JSONArray codes) {
        JSONObject formattedCodes = new JSONObject();
        codes.forEach(c -> {
            JSONObject tempCode = (JSONObject) c;
            String code = tempCode.getString(CODE);
            if (tempCode.has(Constants.PARENTS)) {
                tempCode.remove(Constants.PARENTS);
            }
            formattedCodes.put(code, tempCode);
        });
        codeList.put(CODES, formattedCodes);
    }

    @Override
    public List<CodeList> getDetailedCodesListForSearch(CodeListKind kind)
            throws RmesException, JsonProcessingException {
        JSONArray lists = repoGestion.getResponseAsArray(codeListsQueries.getCodesListsForSearch(kind));
        JSONArray codes = repoGestion.getResponseAsArray(codeListsQueries.getCodesForSearch(kind));

        for (int i = 0; i < lists.length(); i++) {
            JSONObject list = lists.getJSONObject(i);
            list.put(CODES, this.getCodesForList(codes, list));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(lists.toString(), new TypeReference<>() {});
    }

    /**
     * Contrôles de présence restants, pour le seul chemin qui passe encore par ici : les listes
     * partielles.
     * <p>
     * Les listes complètes sont validées à la frontière HTTP par {@code CodesListRequest}
     * (Bean Validation, {@code @NotBlank} sur les huit champs obligatoires) : leurs contrôles de
     * présence — {@code lastClassUriSegment} et {@code lastListUriSegment} compris — ont disparu
     * d'ici, ils faisaient double emploi et ne voyaient de toute façon pas la différence entre une
     * clé absente et une valeur blanche.
     * <p>
     * Ce qui reste : les trois champs que les listes partielles n'ont pas encore de DTO validé pour
     * garder (à migrer avec elles), et la règle « au moins un code », qui a besoin du contexte et
     * n'est pas exprimable en contrainte de champ.
     */
    public void validateCodeList(JSONObject codeList, CodeListKind kind) throws RmesException {
        if (!codeList.has(Constants.ID)) {
            throw new RmesBadRequestException("The id of the list should be defined");
        }
        if (!codeList.has(Constants.LABEL_LG1)) {
            throw new RmesBadRequestException("The labelLg1 of the list should be defined");
        }
        if (!codeList.has(Constants.LABEL_LG2)) {
            throw new RmesBadRequestException("The labelLg2 of the list should be defined");
        }
        if (kind.isPartial()
                && (!codeList.has(CODES)
                        || codeList.getJSONObject(CODES).keySet().isEmpty())) {
            throw new RmesBadRequestException(
                    CodesListErrorCodes.CODE_LIST_AT_LEAST_ONE_CODE, "A code list should contain at least one code");
        }
    }

    private IRI generateIri(JSONObject codesList, CodeListKind kind) {
        if (kind.isPartial()) {
            return RdfUtils.codeListIRI(codesList.getString(Constants.ID));
        } else {
            return RdfUtils.codeListIRI(codesList.getString(LAST_LIST_URI_SEGMENT));
        }
    }

    private boolean checkCodeListUnicity(CodeListKind kind, JSONObject codeList, String iri) throws RmesException {
        String id = codeList.getString(Constants.ID);
        if (!kind.isPartial()) {
            IRI seeAlso = RdfUtils.codeListIRI(CONCEPT + codeList.getString(LAST_CLASS_URI_SEGMENT));
            return repoGestion.getResponseAsBoolean(
                    codeListsQueries.checkCodeListUnicity(id, iri, RdfUtils.toString(seeAlso), CodeListKind.FULL));
        }
        return repoGestion.getResponseAsBoolean(
                codeListsQueries.checkCodeListUnicity(id, iri, "", CodeListKind.PARTIAL));
    }

    @Override
    public String setCodesList(String body, CodeListKind kind) throws RmesException {
        JSONObject codesList = new JSONObject(body);

        this.validateCodeList(codesList, kind);

        IRI codeListIri = this.generateIri(codesList, kind);

        if (this.checkCodeListUnicity(kind, codesList, RdfUtils.toString(codeListIri))) {
            throw new RmesBadRequestException(
                    CodesListErrorCodes.CODE_LIST_UNICITY, "The identifier, IRI and OWL class should be unique", "");
        }

        repoGestion.clearStructureNodeAndComponents(codeListIri);
        Model model = new LinkedHashModel();
        Resource graph = RdfUtils.codesListGraph();
        RdfUtils.addTripleDateTime(codeListIri, DCTERMS.CREATED, DateUtils.getCurrentDate(), model, graph);
        RdfUtils.addTripleDateTime(codeListIri, DCTERMS.MODIFIED, DateUtils.getCurrentDate(), model, graph);
        return this.createOrUpdateCodeList(model, graph, codesList, codeListIri, kind);
    }

    @Override
    public String setCodesList(String id, String body, CodeListKind kind) throws RmesException {
        JSONObject codesList = new JSONObject(body);

        this.validateCodeList(codesList, kind);

        if (!id.equals(codesList.getString(Constants.ID))) {
            throw new RmesBadRequestException(
                    CodesListErrorCodes.CODE_LIST_ID_MISMATCH, "The id of the list should match the id of the url", id);
        }

        IRI codeListIri = this.generateIri(codesList, kind);

        // Lève un 404 si la liste n'existe pas : sans ce contrôle, le PUT était un upsert silencieux.
        // La recherche se fait par IRI et non par notation, car l'identifiant reste renommable.
        JSONObject persistedCodesList =
                repoGestion.getResponseAsObject(codeListsQueries.getCodesListByIri(RdfUtils.toString(codeListIri)));
        if (persistedCodesList.isEmpty()) {
            throw new RmesNotFoundException(CodesListErrorCodes.CODE_LIST_UNKNOWN_ID, "CodeList not found", id);
        }

        repoGestion.clearStructureNodeAndComponents(codeListIri);
        Model model = new LinkedHashModel();
        Resource graph = RdfUtils.codesListGraph();

        // La date de création n'appartient pas au client : on garde celle qui est en base.
        RdfUtils.addTripleDateTime(
                codeListIri, DCTERMS.CREATED, persistedCodesList.optString(Constants.CREATED), model, graph);
        RdfUtils.addTripleDateTime(codeListIri, DCTERMS.MODIFIED, DateUtils.getCurrentDate(), model, graph);

        return this.createOrUpdateCodeList(model, graph, codesList, codeListIri, kind);
    }

    @Override
    public String getPartialCodeListByParent(String parentCode) throws RmesException {
        JSONObject parent = this.getDetailedCodesListJson(parentCode);
        String parentIRI = parent.getString("iri");
        JSONArray partials = repoGestion.getResponseAsArray(codeListsQueries.getPartialCodeListByParentUri(parentIRI));
        return partials.toString();
    }

    @Override
    public void deleteCodeList(String notation, CodeListKind kind) throws RmesException {
        JSONObject codesList = getDetailedPartialCodesListJson(notation);
        String iri = codesList.getString("iri");

        if (!codesList.getString(VALIDATION_STATE).equalsIgnoreCase("Unpublished")) {
            throw new RmesBadRequestException(
                    CodesListErrorCodes.CODE_LIST_DELETE_ONLY_UNPUBLISHED, "Only unpublished codelist can be deleted");
        }

        if (!kind.isPartial()) {
            JSONArray partials = repoGestion.getResponseAsArray(codeListsQueries.getPartialCodeListByParentUri(iri));
            if (!partials.isEmpty()) {
                throw new RmesBadRequestException(
                        CodesListErrorCodes.CODE_LIST_DELETE_CODELIST_WITHOUT_PARTIAL,
                        "Only codelist with partial codelists can be deleted");
            }
            if (codesList.has(CODES)) {
                JSONObject codes = codesList.getJSONObject(CODES);
                for (String key : codes.keySet()) {
                    String codeIri = codes.getJSONObject(key).getString("iri");
                    repoGestion.deleteObject(RdfUtils.toURI(codeIri), null);
                }
            }
        }

        repoGestion.deleteObject(RdfUtils.toURI(iri), null);
    }

    @Override
    public void publishCodeList(String id, CodeListKind kind) throws RmesException {

        JSONObject codesList = getDetailedPartialCodesListJson(id);
        PublicationUtils.rejectIfAlreadyPublished("Codes list", id, codesList.optString(VALIDATION_STATE));

        String iri = codesList.getString("iri");
        IRI codelist = RdfUtils.createIRI(iri);

        codeListPublication.publishCodeListAndCodes(codelist);

        Model model = new LinkedHashModel();
        model.add(
                codelist,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(ValidationStatus.VALIDATED),
                RdfUtils.codesListGraph());
        model.remove(
                codelist,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED),
                RdfUtils.codesListGraph());
        model.remove(
                codelist,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(ValidationStatus.MODIFIED),
                RdfUtils.codesListGraph());

        repoGestion.objectValidation(codelist, model);
    }

    private String createOrUpdateCodeList(
            Model model, Resource graph, JSONObject codesList, IRI codeListIri, CodeListKind kind)
            throws RmesException {
        String codeListId = codesList.getString(Constants.ID);

        if (codesList.has(VALIDATION_STATE)
                && codesList.getString(VALIDATION_STATE).equalsIgnoreCase(ValidationStatus.VALIDATED.getValue())) {
            model.add(codeListIri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), graph);
        } else {
            model.add(
                    codeListIri,
                    INSEE.VALIDATION_STATE,
                    RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED),
                    graph);
        }

        IRI type = kind.isPartial() ? SKOS.COLLECTION : SKOS.CONCEPT_SCHEME;
        RdfUtils.addTripleUri(codeListIri, RDF.TYPE, type, model, graph);
        model.add(codeListIri, SKOS.NOTATION, RdfUtils.setLiteralString(codeListId), graph);

        if (codesList.has("disseminationStatus")) {
            RdfUtils.addTripleUri(
                    codeListIri, INSEE.DISSEMINATIONSTATUS, codesList.getString("disseminationStatus"), model, graph);
        }

        model.add(
                codeListIri,
                SKOS.PREF_LABEL,
                RdfUtils.setLiteralString(codesList.getString(Constants.LABEL_LG1), languages.lg1()),
                graph);
        model.add(
                codeListIri,
                SKOS.PREF_LABEL,
                RdfUtils.setLiteralString(codesList.getString(Constants.LABEL_LG2), languages.lg2()),
                graph);

        if (codesList.has(Constants.DESCRIPTION_LG1)) {
            model.add(
                    codeListIri,
                    SKOS.DEFINITION,
                    RdfUtils.setLiteralString(codesList.getString(Constants.DESCRIPTION_LG1), languages.lg1()),
                    graph);
        }
        if (codesList.has(Constants.DESCRIPTION_LG2)) {
            model.add(
                    codeListIri,
                    SKOS.DEFINITION,
                    RdfUtils.setLiteralString(codesList.getString(Constants.DESCRIPTION_LG2), languages.lg2()),
                    graph);
        }
        if (codesList.has(Constants.CREATOR)) {
            RdfUtils.addTripleUri(codeListIri, DC.CREATOR, codesList.getString(Constants.CREATOR), model, graph);
        }
        if (codesList.has(Constants.CONTRIBUTOR)) {
            codesList
                    .getJSONArray(Constants.CONTRIBUTOR)
                    .toList()
                    .forEach(c -> RdfUtils.addTripleUri(codeListIri, DC.CONTRIBUTOR, (String) c, model, graph));
        }

        if (kind.isPartial()) {
            if (codesList.has(CODES)) {
                JSONObject codes = codesList.getJSONObject(CODES);
                for (String key : codes.keySet()) {
                    JSONObject code = codes.getJSONObject(key);
                    RdfUtils.addTripleUri(
                            codeListIri, SKOS.MEMBER, RdfUtils.createIRI(code.getString("iri")), model, graph);
                }
            }
            if (codesList.has("iriParent")) {
                RdfUtils.addTripleUri(
                        codeListIri, PROV.WAS_DERIVED_FROM, codesList.getString("iriParent"), model, graph);
            }
        } else {
            RdfUtils.addTripleString(
                    codeListIri, INSEE.LAST_CODE_URI_SEGMENT, codesList.getString(LAST_CODE_URI_SEGMENT), model, graph);
            IRI owlClassUri = RdfUtils.codeListIRI(CONCEPT + codesList.getString(LAST_CLASS_URI_SEGMENT));
            RdfUtils.addTripleUri(codeListIri, RDFS.SEEALSO, owlClassUri, model, graph);
            RdfUtils.addTripleUri(owlClassUri, RDF.TYPE, OWL.CLASS, model, graph);
            RdfUtils.addTripleUri(owlClassUri, RDFS.SEEALSO, codeListIri, model, graph);
        }
        repoGestion.loadSimpleObject(codeListIri, model, null);
        return codeListId;
    }

    private void createMainCodeTriplet(
            Resource graph, IRI codeListIri, CodeRequest code, Model codeListModel, IRI codeIri, IRI uriOwlClass) {
        RdfUtils.addTripleUri(codeIri, SKOS.IN_SCHEME, codeListIri, codeListModel, graph);
        RdfUtils.addTripleString(codeIri, SKOS.NOTATION, code.code(), codeListModel, graph);
        RdfUtils.addTripleUri(codeIri, RDF.TYPE, SKOS.CONCEPT, codeListModel, graph);
        RdfUtils.addTripleUri(codeIri, RDF.TYPE, uriOwlClass, codeListModel, graph);

        codeListModel.add(codeIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(code.labelLg1(), languages.lg1()), graph);
        codeListModel.add(codeIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(code.labelLg2(), languages.lg2()), graph);

        if (code.descriptionLg1() != null) {
            codeListModel.add(
                    codeIri, SKOS.DEFINITION, RdfUtils.setLiteralString(code.descriptionLg1(), languages.lg1()), graph);
        }
        if (code.descriptionLg2() != null) {
            codeListModel.add(
                    codeIri, SKOS.DEFINITION, RdfUtils.setLiteralString(code.descriptionLg2(), languages.lg2()), graph);
        }
    }

    private JSONArray getCodesForList(JSONArray codes, JSONObject list) {
        JSONArray codesList = new JSONArray();
        for (int i = 0; i < codes.length(); i++) {
            JSONObject code = codes.getJSONObject(i);
            if (code.getString(Constants.ID).equalsIgnoreCase(list.getString(Constants.ID))) {
                codesList.put(code);
            }
        }
        return codesList;
    }

    @Override
    public String getCodeUri(String notationCodeList, String notationCode) throws RmesException {
        if (StringUtils.isEmpty(notationCodeList) || StringUtils.isEmpty(notationCode)) {
            return null;
        }
        JSONObject code =
                repoGestion.getResponseAsObject(codeListsQueries.getCodeUriByNotation(notationCodeList, notationCode));
        return QueryUtils.correctEmptyGroupConcat(code.getString(Constants.URI));
    }

    @Override
    public List<PartialCodesList> getAllCodesLists(CodeListKind kind) throws RmesException, JsonProcessingException {
        var codeslists = repoGestion.getResponseAsArray(codeListsQueries.getAllCodesLists(kind));
        return DiacriticSorter.sort(codeslists, PartialCodesList[].class, PartialCodesList::labelLg1);
    }

    @Override
    public String getCodesListByIRI(String iri) throws RmesException {
        return repoGestion
                .getResponseAsArray(codeListsQueries.geCodesListByIRI(iri))
                .toString();
    }

    @Override
    public String updateCodeFromCodeList(String notation, String code, CodeRequest body) throws RmesException {
        // La mise à jour est un delete suivi d'un add : sans ce contrôle, un body portant un autre code
        // renommait le code de l'url et écrasait silencieusement un éventuel code homonyme.
        if (!code.equals(body.code())) {
            throw new RmesBadRequestException(
                    CodesListErrorCodes.CODE_LIST_CODE_MISMATCH,
                    "The code of the body should match the code of the url",
                    code);
        }
        this.deleteCodeFromCodeList(notation, code);
        return this.addCodeFromCodeList(notation, body);
    }

    @Override
    public String addCodeFromCodeList(String notation, CodeRequest code) throws RmesException {
        // L'écriture remplace tous les triplets du code : sans cette garde, poster deux fois la même
        // notation écrasait silencieusement le premier code, libellés compris.
        if (!repoGestion
                .getResponseAsObject(codeListsQueries.getCodeByNotation(notation, code.code()))
                .isEmpty()) {
            throw new RmesBadRequestException(
                    CodesListErrorCodes.CODE_LIST_CODE_ALREADY_EXISTS,
                    "Code already exists in this code list",
                    code.code());
        }
        JSONObject codesList = this.getDetailedCodesListJson(notation);

        IRI owlClassUri = RdfUtils.codeListIRI(CONCEPT + codesList.getString(LAST_CLASS_URI_SEGMENT));
        String lastCodeUriSegment = codesList.getString(LAST_CODE_URI_SEGMENT);
        IRI codeIri = RdfUtils.codeListIRI(lastCodeUriSegment + "/" + code.code());
        IRI codeListIri = this.generateIri(codesList, CodeListKind.FULL);

        Model codeModel = new LinkedHashModel();
        createMainCodeTriplet(RdfUtils.codesListGraph(), codeListIri, code, codeModel, codeIri, owlClassUri);

        repoGestion.loadSimpleObject(codeIri, codeModel, null);

        return code.code();
    }

    @Override
    public String deleteCodeFromCodeList(String notation, String code) throws RmesException {
        if (repoGestion
                .getResponseAsObject(codeListsQueries.getCodeByNotation(notation, code))
                .isEmpty()) {
            throw new RmesNotFoundException(
                    CodesListErrorCodes.CODE_LIST_UNKNOWN_CODE, "Code not found in this code list", code);
        }
        JSONObject codesList = this.getDetailedCodesListJson(notation);
        String lastCodeUriSegment = codesList.getString(LAST_CODE_URI_SEGMENT);
        IRI codeIri = RdfUtils.codeListIRI(lastCodeUriSegment + "/" + code);
        repoGestion.deleteObject(codeIri, null);
        return null;
    }
}
