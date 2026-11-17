package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.model.operations.documentations.Document;
import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.utils.UriUtils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DocumentsUtils extends RdfService {
    private static final String SCHEME_FILE = "file://";
    static final Logger logger = LoggerFactory.getLogger(DocumentsUtils.class);
    public static final Pattern VALID_FILENAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z]+$");

    private final FilesOperations filesOperations;
    private final StorageProperties storageProperties;

    private final OperationDocumentsQueries operationDocumentsQueries;

    public DocumentsUtils(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            PublicationUtils publicationUtils,
            FilesOperations filesOperations,
            StorageProperties storageProperties,
            OperationDocumentsQueries operationDocumentsQueries) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.filesOperations = filesOperations;
        this.storageProperties = storageProperties;
        this.operationDocumentsQueries = operationDocumentsQueries;
    }

    /*
     * METHODS LINKS TO THE SIMS - RUBRICS
     */
    public void addDocumentsAndLinksToRubric(Model model, Resource graph, List<Document> documents, IRI textUri)
            throws RmesException {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        List<Value> docUris = new ArrayList<>(documents.size());
        for (Document doc : documents) {
            if (StringUtils.isNotEmpty(doc.getUri())) {
                docUris.add(RdfUtils.toURI(doc.getUri()));
            } else {
                docUris.add(getDocumentUri(RdfUtils.toURI(doc.getUrl())));
            }
        }
        Resource head = RdfUtils.createBlankNode();
        Model listModel = new LinkedHashModel();
        RDFCollections.asRDF(docUris, head, listModel);
        listModel.forEach(st -> model.add(st.getSubject(), st.getPredicate(), st.getObject(), graph));
        model.add(textUri, INSEE.ADDITIONALMATERIAL, head, graph);
    }

    /**
     * Get documents link to one rubric of a metadata report
     *
     * @param idSims
     * @param idRubric
     * @return
     * @throws RmesException
     */
    public JSONArray getListDocumentLink(String idSims, String idRubric, String lang) throws RmesException {
        JSONArray allDocs = repoGestion.getResponseAsArray(operationDocumentsQueries.getDocumentsForSimsRubricQuery(
                idSims, idRubric, "http://bauhaus/codes/langue/" + lang));
        JSONArray ordered = RdfListOrderer.orderByList(allDocs, "listCell", "listNext");
        formatDateInJsonArray(ordered);
        return ordered;
    }

    /**
     * Get documents link to a metadata report (no links)
     *
     * @param idSims
     * @return
     * @throws RmesException
     */
    public JSONArray getListDocumentSims(String idSims) throws RmesException {
        JSONArray allDocs = repoGestion.getResponseAsArray(operationDocumentsQueries.getDocumentsForSimsQuery(idSims));
        formatDateInJsonArray(allDocs);
        return allDocs;
    }

    /**
     * Get links link to a metadata report (no document)
     *
     * @param idSims
     * @return
     * @throws RmesException
     */
    public JSONArray getListLinksSims(String idSims) throws RmesException {
        JSONArray allLinks = repoGestion.getResponseAsArray(operationDocumentsQueries.getLinksForSimsQuery(idSims));
        formatDateInJsonArray(allLinks);
        return allLinks;
    }

    private void formatDateInJsonArray(JSONArray allDocs) {
        if (!allDocs.isEmpty()) {
            JSONUtils.stream(allDocs).forEach(this::formatDateInJsonObject);
        }
    }

    private void formatDateInJsonObject(JSONObject doc) {
        if (doc.has(Constants.UPDATED_DATE)) {
            String formatedDate = DateUtils.getDate(doc.getString(Constants.UPDATED_DATE));
            doc.remove(Constants.UPDATED_DATE);
            doc.put(Constants.UPDATED_DATE, formatedDate);
        }
    }

    /**
     * Generate a new ID for document or link
     *
     * @return
     * @throws RmesException
     */
    protected String createDocumentID() throws RmesException {
        logger.info("Generate document id");

        JSONObject json = repoGestion.getResponseAsObject(operationDocumentsQueries.lastDocumentID());
        int id = getIdFromJson(json) == null ? 999 : getIdFromJson(json);

        json = repoGestion.getResponseAsObject(operationDocumentsQueries.lastLinkID());
        id = (getIdFromJson(json) == null ? id : Math.max(getIdFromJson(json), id)) + 1;
        return Integer.toString(id);
    }

    public Integer getIdFromJson(JSONObject json) {
        if (json.isEmpty()) {
            return null;
        } else {
            String id = json.getString(Constants.ID);
            if (id.equals(Constants.UNDEFINED) || StringUtils.isEmpty(id)) {
                return null;
            } else {
                return Integer.parseInt(id);
            }
        }
    }

    public JSONArray getDocumentsUriAndUrlForSims(String id) throws RmesException {
        logger.debug("Querrying the list of uri and url for all documents for the SIMS {}", id);
        return repoGestion.getResponseAsArray(operationDocumentsQueries.getDocumentsUriAndUrlForSims(id));
    }

    // Check that the document is not referred to by any sims

    public static String getDocumentNameFromUrl(String docUrl) {
        return UriUtils.getLastPartFromUri(docUrl);
    }

    /**
     * return new uri if url doesn't exist
     *
     * @param url
     * @return
     * @throws RmesException
     */
    private IRI getDocumentUri(IRI url) throws RmesException {
        JSONObject uri = repoGestion.getResponseAsObject(
                operationDocumentsQueries.getDocumentUriQuery(getDocumentNameFromUrl(url.stringValue())));
        if (uri.isEmpty() || !uri.has(Constants.DOCUMENT)) {
            String id = createDocumentID();
            return RdfUtils.objectIRI(ObjectType.DOCUMENT, id);
        }
        return RdfUtils.toURI(uri.getString(Constants.DOCUMENT));
    }

    public static String getDocumentUrlFromDocument(JSONObject jsonDoc) {
        return jsonDoc.getString(Constants.URL).replace(SCHEME_FILE, "");
    }

    public Document buildDocumentFromJson(JSONObject jsonDoc) {
        Document doc = new Document();
        if (jsonDoc.has(Constants.LABEL_LG1)) {
            doc.setLabelLg1(jsonDoc.getString(Constants.LABEL_LG1));
        }
        if (jsonDoc.has(Constants.LABEL_LG2)) {
            doc.setLabelLg2(jsonDoc.getString(Constants.LABEL_LG2));
        }
        if (jsonDoc.has(Constants.DESCRIPTION_LG1)) {
            doc.setDescriptionLg1(jsonDoc.getString(Constants.DESCRIPTION_LG1));
        }
        if (jsonDoc.has(Constants.DESCRIPTION_LG2)) {
            doc.setDescriptionLg2(jsonDoc.getString(Constants.DESCRIPTION_LG2));
        }
        if (jsonDoc.has(Constants.UPDATED_DATE)) {
            doc.setDateMiseAJour(jsonDoc.getString(Constants.UPDATED_DATE));
        }
        if (jsonDoc.has(Constants.LANG)) {
            doc.setLangue(jsonDoc.getString(Constants.LANG));
        }
        if (jsonDoc.has(Constants.URL)) {
            doc.setUrl(jsonDoc.getString(Constants.URL));
        }
        if (jsonDoc.has(Constants.URI)) {
            doc.setUri(jsonDoc.getString(Constants.URI));
        }

        return doc;
    }

    public InputStream retrieveDocumentFromStorage(String filename) {
        return filesOperations.read(new fr.insee.rmes.modules.commons.domain.model.Document(
                storageProperties.directoryGestion(), filename));
    }

    public boolean existsInStorage(String filename) {
        return filesOperations.exists(new fr.insee.rmes.modules.commons.domain.model.Document(
                storageProperties.directoryGestion(), filename));
    }
}
