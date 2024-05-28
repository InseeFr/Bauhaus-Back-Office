package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.exceptions.RmesException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DocumentsImpl implements DocumentsService {

	private static final  Logger logger = LoggerFactory.getLogger(DocumentsImpl.class);

	final DocumentsUtils documentsUtils;
	
	public DocumentsImpl(DocumentsUtils documentsUtils) {
		this.documentsUtils = documentsUtils;
	}

	/*
	 * Get 
	 */
	
	@Override
	public String getDocuments() throws RmesException {
		logger.debug("Starting to get documents list");
		return documentsUtils.getAllDocuments().toString();
	}

	@Override
	public JSONObject getDocument(String id) throws RmesException {
		logger.debug("Starting to get document {} ", id);
		return documentsUtils.getDocument(id, false);
	}
	
	@Override
	public Object getLink(String id) throws RmesException {
		logger.debug("Starting to get link {} ", id);
		return documentsUtils.getDocument(id, true);
	}

	/**
	 * Method to create a new document and upload the corresponding file at the same time.
	 *
	 * @param body the metadata of the document we want to create
	 * @param documentFile the file that will be linked to the document
	 * @param documentName the name of file
	 * @return the identifier of the newly created document
	 * @throws fr.insee.rmes.exceptions.RmesNotAcceptableException if the name of the file is not correct
	 * @throws RmesException
	 */
	@Override
	public String createDocument(String body, InputStream documentFile, String documentName) throws RmesException {
		logger.debug("Creating document {}", documentName);
		documentsUtils.checkFileNameValidity(documentName);

		String id = documentsUtils.createDocumentID();
		logger.debug("Creating document {} with the identifier {}", documentName, id);

		documentsUtils.createDocument(id, body,false, documentFile,documentName);
		return id;
	}

	/**
	 * Method to change the uploaded file of an existing document.
	 *
	 * @param docId the identifier of the document we want to change the file
	 * @param documentFile the file that will be linked to the document
	 * @param documentName the name of file
	 * @return the url of the newly uploaded file
	 * @throws fr.insee.rmes.exceptions.RmesNotAcceptableException if the name of the file is not correct
	 * @throws RmesException
	 */
	@Override
	public String changeDocument(String docId, InputStream documentFile, String documentName)
			throws RmesException {

		logger.debug("Updating document {} with the identifier {}", documentName, docId);
		documentsUtils.checkFileNameValidity(documentName);
		return documentsUtils.changeFile(docId,documentFile,documentName);
	}

	/*
	 * Update
	 * @see fr.insee.rmes.bauhaus_services.DocumentsService#setDocument(java.lang.String)
	 */
	@Override
	public String setDocument(String id, String body) throws RmesException {
		documentsUtils.setDocument(id, body, false);
		return id;
	}

	/*
	 * Delete 
	 */
	@Override
	public HttpStatus deleteDocument(String id) throws RmesException {
		return documentsUtils.deleteDocument(id, false);
	}

	@Override
	public ResponseEntity<Object> downloadDocument(String id) throws RmesException, IOException {
		return documentsUtils.downloadDocumentFile(id);	
	}
	
	/*
	 * LINKS
	 */
	
	/**
	 * Create new link
	 */
	@Override
	public String setLink(String body) throws RmesException {
		String id = documentsUtils.createDocumentID();
		logger.debug("Create document : {}", id);
		documentsUtils.createDocument(id,body,true, null, null);
		return id;
	}
	
	/**
	 * Update a link
	 * @return 
	 */
	@Override
	public String setLink(String id, String body) throws RmesException {
		documentsUtils.setDocument(id,body, true);
		return id;
	}

	@Override
	public HttpStatus deleteLink(String id) throws RmesException {
		return documentsUtils.deleteDocument(id, true);

	}
}
