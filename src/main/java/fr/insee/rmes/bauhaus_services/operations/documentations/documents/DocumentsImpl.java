package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.exceptions.RmesException;

@Service
public class DocumentsImpl implements DocumentsService {

	private static final  Logger logger = LogManager.getLogger(DocumentsImpl.class);

	@Autowired 
	DocumentsUtils documentsUtils;
	
	public DocumentsImpl() {
		//Utility class
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

	/*
	 * Create
	 * @see fr.insee.rmes.bauhaus_services.DocumentsService#createDocument(java.lang.String)
	 */
	@Override
	public String createDocument(String body, InputStream documentFile, String documentName) throws RmesException {
		logger.debug("Creating document {}", documentName);
		documentsUtils.checkFileNameValidity(documentName);

		String id = documentsUtils.createDocumentID();
		logger.debug("Creating document {} with the identifier {}", documentName, id);

		documentsUtils.createDocument(id,body,false, documentFile,documentName);
		return id;
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

	/*
	 * Change an uploaded document 
	 * Keep the document links
	 */
	@Override
	public String changeDocument(String docId, InputStream documentFile, String documentName)
			throws RmesException {
		return documentsUtils.changeFile(docId,documentFile,documentName);		
	}	
	

	@Override
	public ResponseEntity<Object> downloadDocument(String id) throws RmesException, IOException  {
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
		String id=documentsUtils.createDocumentID();
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
