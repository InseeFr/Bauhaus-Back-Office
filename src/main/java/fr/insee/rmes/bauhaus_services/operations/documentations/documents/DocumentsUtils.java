package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.FilesOperations;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.*;
import fr.insee.rmes.model.operations.documentations.Document;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.PAV;
import fr.insee.rmes.persistance.ontologies.SCHEMA;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentsQueries;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.UriUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DocumentsUtils extends RdfService {

    private static final String SCHEME_FILE = "file://";
    static final Logger logger = LoggerFactory.getLogger(DocumentsUtils.class);
    public static final Pattern VALID_FILENAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z]+$");

    private final ParentUtils ownersUtils;

    private final FilesOperations filesOperations;

    public DocumentsUtils(ParentUtils ownersUtils, FilesOperations filesOperations) {
        this.ownersUtils = ownersUtils;
        this.filesOperations = filesOperations;
    }

    /*
     * METHODS LINKS TO THE SIMS - RUBRICS
     */
    public void addDocumentsAndLinksToRubric(Model model, Resource graph, List<Document> documents, IRI textUri)
            throws RmesException {
        if (documents != null && !documents.isEmpty()) {
            for (Document doc : documents) {
                IRI url = RdfUtils.toURI(doc.getUrl());
                IRI docUri;
                if (StringUtils.isNotEmpty(doc.getUri())) {
                    docUri = RdfUtils.toURI(doc.getUri());
                } else {
                    docUri = getDocumentUri(url);
                }
                RdfUtils.addTripleUri(textUri, INSEE.ADDITIONALMATERIAL, docUri, model, graph);
            }
        }
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
        JSONArray allDocs = repoGestion.getResponseAsArray(DocumentsQueries.getDocumentsForSimsRubricQuery(idSims, idRubric, "http://bauhaus/codes/langue/" + lang));
        formatDateInJsonArray(allDocs);
        return allDocs;
    }

    /**
     * Get documents link to a metadata report (no links)
     *
     * @param idSims
     * @return
     * @throws RmesException
     */
    public JSONArray getListDocumentSims(String idSims) throws RmesException {
        JSONArray allDocs = repoGestion.getResponseAsArray(DocumentsQueries.getDocumentsForSimsQuery(idSims));
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
        JSONArray allLinks = repoGestion.getResponseAsArray(DocumentsQueries.getLinksForSimsQuery(idSims));
        formatDateInJsonArray(allLinks);
        return allLinks;
    }

    /**
     * Get all documents
     *
     * @return allDocs
     * @throws RmesException
     */
    public JSONArray getAllDocuments() throws RmesException {
        JSONArray allDocs = new JSONArray();
        try {
            allDocs = repoGestion.getResponseAsArray(DocumentsQueries.getAllDocumentsQuery());
            formatDateInJsonArray(allDocs);
        } catch (RmesException e) {
            logger.error(e.getMessage());
            throw e;
        }
        return allDocs;
    }

    private void formatDateInJsonArray(JSONArray allDocs) {
        if (!allDocs.isEmpty()) {
            for (int i = 0; i < allDocs.length(); i++) {
                JSONObject doc = allDocs.getJSONObject(i);
                formatDateInJsonObject(doc);
            }
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

        JSONObject json = repoGestion.getResponseAsObject(DocumentsQueries.lastDocumentID());
        int id = getIdFromJson(json) == null ? 999 : getIdFromJson(json);

        json = repoGestion.getResponseAsObject(DocumentsQueries.lastLinkID());
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


    /**
     * Create document
     *
     * @param id
     * @param body
     * @param documentFile
     * @throws RmesException
     */
    public void createDocument(String id, String body, boolean isLink, InputStream documentFile, String documentName)
            throws RmesException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Document document = new Document(id, isLink);

        try {
            document = mapper.readerForUpdating(document).readValue(body);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }


        validate(document);


        if (isLink) {
            checkLinkDoesNotExist(id, document.getUrl());
        } else {
            String url = createFileUrl(documentName);
            checkDocumentDoesNotExist(id, url);

            logger.info("URL CREATED : {}", url);
            document.setUrl(url);

            uploadFile(documentFile, documentName, url, false);
        }

        //Write RDF graph in database
        try {
            IRI docUri = RdfUtils.toURI(document.getUri());
            writeRdfDocument(document, docUri);
        } catch (RmesException e) {
            deleteDocument(id, isLink);
            throw e;
        }
    }


    private void checkLinkDoesNotExist(String id, String url) throws RmesException {
        if (StringUtils.isEmpty(url)) {
            logger.debug("The Link {} must have a non-empty URL", id);
            throw new RmesNotAcceptableException(ErrorCodes.LINK_EMPTY_URL, "A link must have a non-empty url. ", id);
        }

        try {
            URI.create(url).toURL();
        } catch (MalformedURLException e) {
            logger.debug("The Link {} is not valid", id);
            throw new RmesNotAcceptableException(ErrorCodes.LINK_BAD_URL, "A link must be a valid url. ", id);
        }


        // Check if the url is already used by a link
        checkUrlDoesNotExist(id, url, ErrorCodes.LINK_EXISTING_URL, "This url is already referenced by another link.");
    }

    private void checkDocumentDoesNotExist(String id, String url) throws RmesException {
        if (StringUtils.isEmpty(url)) {
            throw new RmesNotAcceptableException(ErrorCodes.DOCUMENT_EMPTY_NAME, "A document must have a non-empty url. ", id);
        }
        if (url.contains(SCHEME_FILE)) {
            url = url.replace(SCHEME_FILE, "");
        }

        // Check if the url is already used by another document
        checkUrlDoesNotExist(id, getDocumentNameFromUrl(url), ErrorCodes.DOCUMENT_CREATION_EXISTING_FILE, "This url is already referenced by another document.");
    }


    private void checkUrlDoesNotExist(String id, String url, int errorCode, String errorMessage) throws RmesException {
        JSONObject existingUriJson = repoGestion.getResponseAsObject(DocumentsQueries.getDocumentUriQuery(url));
        if (!existingUriJson.isEmpty()) {
            String uri = existingUriJson.getString(Constants.DOCUMENT);
            String existingId = getDocumentNameFromUrl(uri);
            if (!existingId.equals(id)) {
                throw new RmesNotAcceptableException(errorCode, errorMessage, uri);
            }
        }
    }


    /**
     * Update a document or link
     *
     * @throws RmesException
     */
    public void setDocument(String id, String body, boolean isLink) throws RmesException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Document document = new Document(id, isLink);

        try {
            document = mapper.readerForUpdating(document).readValue(body);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }


        logger.info("Update document : {} - {} / {}", document.getId(), document.getLabelLg1(), document.getLabelLg2());
        validate(document);

        IRI docUri = isLink ? RdfUtils.linkIRI(id) : RdfUtils.documentIRI(id);
        writeRdfDocument(document, docUri);
    }

    public JSONArray getDocumentsUriAndUrlForSims(String id) throws RmesException {
        logger.debug("Querrying the list of uri and url for all documents for the SIMS {}", id);
        JSONArray documents = repoGestion.getResponseAsArray(DocumentsQueries.getDocumentsUriAndUrlForSims(id));
        return documents;
    }

    /**
     * Get RDF for a document or a link by ID
     * with associated sims (and their creators)
     *
     * @param id
     * @param isLink
     * @return
     * @throws RmesException
     */
    public JSONObject getDocument(String id, boolean isLink) throws RmesException {
        logger.debug("Querrying the Database in order to get the document/link {}", id);
        JSONObject jsonDocs = new JSONObject();
        try {
            jsonDocs = repoGestion.getResponseAsObject(DocumentsQueries.getDocumentQuery(id, isLink));
        } catch (RmesException e) {
            logger.error("Error when querrying the database for the document/link {}", id);
        }

        if (jsonDocs.isNull(Constants.URI)) {
            logger.error("Error with the document {}. It looks like it does not have an uri", id);
            throw new RmesNotFoundException(ErrorCodes.DOCUMENT_UNKNOWN_ID, "Cannot find " + (isLink ? "Link" : "Document") + " with id : " + id, id);
        }
        formatDateInJsonObject(jsonDocs);
        jsonDocs.put("sims", this.getSimsByDocument(id, isLink));
        return jsonDocs;
    }

    private JSONArray getSimsByDocument(String id, Boolean isLink) throws RmesException {
        JSONArray sims = repoGestion.getResponseAsArray(DocumentsQueries.getSimsByDocument(id, isLink));

        for (int i = 0; i < sims.length(); i++) {
            JSONObject sim = sims.getJSONObject(i);
            sim.put(Constants.CREATORS, new JSONArray(ownersUtils.getDocumentationOwnersByIdSims(sim.getString(Constants.ID))));
        }
        return sims;
    }

    /**
     * Delete a document or a link
     *
     * @param docId
     * @return
     * @throws RmesException
     */
    public HttpStatus deleteDocument(String docId, boolean isLink) throws RmesException {
        JSONObject jsonDoc = getDocument(docId, isLink);
        String uri = jsonDoc.getString(Constants.URI);
        String url = getDocumentUrlFromDocument(jsonDoc);
        IRI docUri = RdfUtils.toURI(uri);

        // Check that the document is not referred to by any sims
        checkDocumentReference(docId, uri);
        // remove the physical file
        if (!isLink) {
            deleteFile(url);
        }
        // delete the Document in the rdf base
        return repoGestion.executeUpdate(DocumentsQueries.deleteDocumentQuery(docUri));
    }

    // Check that the document is not referred to by any sims
    private void checkDocumentReference(String docId, String uri) throws RmesException {
        JSONArray jsonResultat = repoGestion.getResponseAsArray(DocumentsQueries.getLinksToDocumentQuery(docId));
        if (!jsonResultat.isEmpty()) {
            throw new RmesBadRequestException(ErrorCodes.DOCUMENT_DELETION_LINKED,
                    "The document " + uri + "cannot be deleted because it is referred to by " + jsonResultat.length()
                            + " sims, including: " + ((JSONObject) jsonResultat.get(0)).get(Constants.TEXT).toString(),
                    jsonResultat);
        }
    }

    public String changeFile(String docId, InputStream documentFile, String documentName) throws RmesException {

        JSONObject jsonDoc = getDocument(docId, false);
        // Cannot upload file for a Link = if not found it's probably a link
        if (!jsonDoc.has(Constants.URL)) {
            throw new RmesException(HttpStatus.NOT_ACCEPTABLE.value(),
                    "Document not found. Warning : Links have no attached file. Cannot upload file " + documentName + " for this document: ", docId);
        }

        String docUrl = getDocumentUrlFromDocument(jsonDoc);


        // Warning if different file extension
        String oldExt = StringUtils.substringAfterLast(docUrl, ".");
        String newExt = StringUtils.substringAfterLast(documentName, ".");
        if (!oldExt.equals(newExt)) {
            logger.info("Warning: The new file has extension: .{} while the old file had extension: .{}", newExt, oldExt);
        }

        String oldName = getDocumentNameFromUrl(docUrl);
        String newUrl = null;

        // Same documentName -> keep the same URL
        if (oldName.equals(documentName)) {
            logger.info("Replacing file {} at the same Url", documentName);
            uploadFile(documentFile, documentName, docUrl, true);
        }
        // Different documentName -> create a new URL
        else {
            // Delete the old file
            logger.info("Delete old file {}, with URL {}", documentName, docUrl);
            deleteFile(docUrl);

            // Upload the new file
            newUrl = createFileUrl(documentName);
            logger.info("Try to replace file {}, new URL is {}", documentName, newUrl);
            uploadFile(documentFile, documentName, newUrl, false);
            // Update document's url
            changeDocumentsURL(jsonDoc.getString(Constants.URI), addSchemeFile(docUrl), addSchemeFile(newUrl));
        }

        return newUrl;
    }

    private String addSchemeFile(String url) {
        if (url.startsWith(SCHEME_FILE)) return url;
        return SCHEME_FILE + url;
    }

    private void uploadFile(InputStream documentFile, String documentName, String url, boolean sameName)
            throws RmesBadRequestException {
        // upload file in storage folder
        logger.debug("URL : {}", url);
        Path path = Paths.get(url.replace(SCHEME_FILE, ""));
        logger.debug("PATH : {}", path);
        if (!sameName && path.toFile().exists()) {
            throw new RmesBadRequestException(ErrorCodes.DOCUMENT_CREATION_EXISTING_FILE,
                    "There is already a document with that name.", documentName);
        }
        filesOperations.writeToDirectoryGestion(documentFile, path);
        // don't throw an error if a file already exists under this name
    }


    private void validate(Document document) throws RmesException {
        if (repoGestion.getResponseAsBoolean(DocumentsQueries.checkLabelUnicity(document.getId(), document.getLabelLg1(), config.getLg1()))) {
            throw new RmesBadRequestException(ErrorCodes.OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG1, "This labelLg1 is already used by another document or link.");
        }
        if (repoGestion.getResponseAsBoolean(DocumentsQueries.checkLabelUnicity(document.getId(), document.getLabelLg2(), config.getLg2()))) {
            throw new RmesBadRequestException(ErrorCodes.OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG2, "This labelLg2 is already used by another document or link.");
        }
    }

    private void writeRdfDocument(Document document, IRI docUri) throws RmesException {
        Resource graph = RdfUtils.documentsGraph();
        Model model = new LinkedHashModel();

        RdfUtils.addTripleUri(docUri, RDF.TYPE, FOAF.DOCUMENT, model, graph);

        String uriString = document.getUrl();

        logger.debug("Add to {} schema:url {}", docUri, uriString);
        RdfUtils.addTripleUri(docUri, SCHEMA.URL, uriString, model, graph);

        if (StringUtils.isNotEmpty(document.getLabelLg1())) {
            logger.debug("Add to {} RDFS:LABEL {}", docUri, document.getLabelLg1());
            RdfUtils.addTripleString(docUri, RDFS.LABEL, document.getLabelLg1(), config.getLg1(), model, graph);
        }
        if (StringUtils.isNotEmpty(document.getLabelLg2())) {
            logger.debug("Add to {} RDFS:LABEL {}", docUri, document.getLabelLg2());
            RdfUtils.addTripleString(docUri, RDFS.LABEL, document.getLabelLg2(), config.getLg2(), model, graph);
        }
        if (StringUtils.isNotEmpty(document.getDescriptionLg1())) {
            logger.debug("Add to {} RDFS.COMMENT {}", docUri, document.getDescriptionLg1());
            RdfUtils.addTripleString(docUri, RDFS.COMMENT, document.getDescriptionLg1(), config.getLg1(), model, graph);
        }
        if (StringUtils.isNotEmpty(document.getDescriptionLg2())) {
            logger.debug("Add to {} RDFS.COMMENT {}", docUri, document.getDescriptionLg2());
            RdfUtils.addTripleString(docUri, RDFS.COMMENT, document.getDescriptionLg2(), config.getLg2(), model, graph);
        }
        if (StringUtils.isNotEmpty(document.getLangue())) {
            logger.debug("Add to {} DC.LANGUAGE {}", docUri, document.getLangue());
            RdfUtils.addTripleString(docUri, DC.LANGUAGE, document.getLangue(), model, graph);
        }
        if (StringUtils.isNotEmpty(document.getDateMiseAJour())) {
            var dateMiseAJour = document.getDateMiseAJour();
            logger.debug("Add to {} PAV.LASTREFRESHEDON {}", docUri, dateMiseAJour);
            RdfUtils.addTripleDate(docUri, PAV.LASTREFRESHEDON, dateMiseAJour, model, graph);
        }
        repoGestion.loadSimpleObject(docUri, model);
    }

    private void changeDocumentsURL(String iri, String docUrl, String newUrl) throws RmesException {
        repoGestion.executeUpdate(DocumentsQueries.changeDocumentUrlQuery(iri, docUrl, newUrl));
    }

    private void deleteFile(String docUrl) {
        Path path = Paths.get(docUrl);
        filesOperations.delete(path);
    }

    public static String getDocumentNameFromUrl(String docUrl) {
        return UriUtils.getLastPartFromUri(docUrl);
    }

    private String createFileUrl(String name) throws RmesException {
        Path gestionStorageFolder=Path.of(config.getDocumentsStorageGestion());
        if (!filesOperations.dirExists(gestionStorageFolder)){
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Storage folder not found",
                    "config.DOCUMENTS_STORAGE");
        }
        String url= gestionStorageFolder.resolve(name).toString();
        Pattern p = Pattern.compile("^(?:[a-zA-Z]+:/)");
        Matcher m = p.matcher(url);
        if (m.find()) {// absolute URL
            return url;
        }
        return addSchemeFile(url);
    }

    /**
     * return new uri if url doesn't exist
     *
     * @param url
     * @return
     * @throws RmesException
     */
    private IRI getDocumentUri(IRI url) throws RmesException {
        JSONObject uri = repoGestion
                .getResponseAsObject(DocumentsQueries.getDocumentUriQuery(getDocumentNameFromUrl(url.stringValue())));
        if (uri.isEmpty() || !uri.has(Constants.DOCUMENT)) {
            String id = createDocumentID();
            return RdfUtils.objectIRI(ObjectType.DOCUMENT, id);
        }
        return RdfUtils.toURI(uri.getString(Constants.DOCUMENT));
    }

    public static String getDocumentUrlFromDocument(JSONObject jsonDoc) {
        return jsonDoc.getString(Constants.URL).replace(SCHEME_FILE, "");
    }

    public void checkFileNameValidity(String fileName) throws RmesNotAcceptableException {
        logger.debug("Checking File Name {}", fileName);
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.debug("The File name is null or empty");
            throw new RmesNotAcceptableException(ErrorCodes.DOCUMENT_EMPTY_NAME, "Empty fileName", fileName);
        }

        Matcher m = VALID_FILENAME_PATTERN.matcher(fileName);
        if (!m.matches()) {
            logger.info("There is a forbidden character in the FileName ");
            throw new RmesNotAcceptableException(ErrorCodes.DOCUMENT_FORBIDDEN_CHARACTER_NAME,
                    "FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens",
                    fileName);
        }

        logger.debug("The file name {} is valid", fileName);
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

    protected String getDocumentFilename(String id) throws RmesException {
        JSONObject jsonDoc = getDocument(id, false);
        String url = getDocumentUrlFromDocument(jsonDoc);
        return getDocumentNameFromUrl(url);
    }

    /**
     * Download a document by id
     *
     * @param id
     * @return Response containing the file (inputStream)
     * @throws RmesException
     */
    public ResponseEntity<org.springframework.core.io.Resource> downloadDocumentFile(String id) throws RmesException {
        String filePath = getDocumentFilename(id);

        try (InputStream inputStream = filesOperations.readInDirectoryGestion(filePath)) { // Lire via l'abstraction et utiliser try-with-resources
            byte[] data = StreamUtils.copyToByteArray(inputStream); // Convertir InputStream en byte[]

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + getFileName(filePath) + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            // return the response with document
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new ByteArrayResource(data));
        }catch (NoSuchFileException e){
            throw new RmesNotFoundException(HttpStatus.NOT_FOUND.value(), filePath+" not found", filePath+" not found");
        }catch (IOException e) {
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "I/O error", "Error downloading file");
        }
    }



    private String getFileName(String path) {
        // Extraire juste le nom de fichier du chemin
        return Paths.get(path).getFileName().toString();
    }

    public InputStream retrieveDocumentFromStorage(String filename) {
        return filesOperations.readInDirectoryGestion(filename);
    }

    public boolean existsInStorage(String filename) {
        return filesOperations.existsInStorageGestion(filename);
    }
}

