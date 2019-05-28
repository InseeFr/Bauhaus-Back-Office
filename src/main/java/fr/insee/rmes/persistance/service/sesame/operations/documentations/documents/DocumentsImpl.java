package fr.insee.rmes.persistance.service.sesame.operations.documentations.documents;

import java.io.InputStream;

import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.DocumentsService;
import fr.insee.rmes.persistance.service.sesame.concepts.ConceptsImpl;

@Service
public class DocumentsImpl implements DocumentsService {

	final static Logger logger = LogManager.getLogger(ConceptsImpl.class);

	@Autowired 
	DocumentsUtils documentsUtils;

	public DocumentsImpl() {
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
		logger.debug("Starting to get document " + id);
		return documentsUtils.getDocument(id);
	}

	/*
	 * Create
	 * @see fr.insee.rmes.persistance.service.DocumentsService#setDocument(java.lang.String)
	 */
	@Override
	public String setDocument(String body, InputStream documentFile,String documentName) throws RmesException {
		String id=documentsUtils.createDocumentID();
		logger.debug("Create document : "+ id);
		documentsUtils.createDocument(id,body,documentFile,documentName);
		return id;
	}

	/*
	 * Update
	 * @see fr.insee.rmes.persistance.service.DocumentsService#setDocument(java.lang.String)
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
	public String setLink(String body) throws RmesException {
		String id=documentsUtils.createDocumentID();
		logger.debug("Create document : "+ id);
		documentsUtils.createLink(id,body);
		return id;
	}

	
}
