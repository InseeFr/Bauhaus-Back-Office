package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
		return documentsUtils.getDocument(id);
	}

	/*
	 * Create
	 * @see fr.insee.rmes.bauhaus_services.DocumentsService#setDocument(java.lang.String)
	 */
	@Override
	public String setDocument(String body, InputStream documentFile,String documentName) throws RmesException {
		documentsUtils.checkFileNameValidity(documentName);
		String id=documentsUtils.createDocumentID();
		logger.debug("Create document : {}", id);
		documentsUtils.createDocument(id,body,documentFile,documentName);
		return id;
	}

	/*
	 * Update
	 * @see fr.insee.rmes.bauhaus_services.DocumentsService#setDocument(java.lang.String)
	 */
	@Override
	public String setDocument(String id, String body) throws RmesException {
		documentsUtils.setDocument(id,body);
		return id;
	}

	/*
	 * Delete 
	 */
	@Override
	public Status deleteDocument(String id) throws RmesException {
		return documentsUtils.deleteDocument(id);
	}

	/*
	 * Change an uploaded document 
	 * Keep the document links
	 */
	@Override
	public String changeDocument(String docId, InputStream documentFile, String documentName)
			throws RmesException {
		return documentsUtils.changeDocument(docId,documentFile,documentName);		
	}	
	
	/*
	 * LINKS
	 */
	
	/*
	 * Create new link
	 */
	@Override
	public String setLink(String body) throws RmesException {
		String id=documentsUtils.createDocumentID();
		logger.debug("Create document : {}", id);
		documentsUtils.createLink(id,body);
		return id;
	}

	@Override
	public Response downloadDocument(String id) throws RmesException, IOException  {
		return documentsUtils.downloadDocument(id);	
	}

	
}
