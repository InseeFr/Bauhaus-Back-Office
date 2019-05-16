package fr.insee.rmes.persistance.service.sesame.operations.documentations.documents;

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
		logger.info("Starting to get documents list");
		String resQuery = documentsUtils.getAllDocuments().toString();
		return resQuery;
	}

	@Override
	public JSONObject getDocument(String id) throws RmesException {
		logger.info("Starting to get document " + id);
		/*String resQuery = documentsUtils.getDocument(id).toString();
		return resQuery;*/
		return documentsUtils.getDocument(id);
	}

	/*
	 * Create
	 * @see fr.insee.rmes.persistance.service.DocumentsService#setDocument(java.lang.String)
	 */
	@Override
	public String setDocument(String body, java.io.InputStream documentFile) throws RmesException {
		String id=documentsUtils.createDocumentID();
		System.out.println("--------------\n doc_id:" + id + "--------------\n");
		documentsUtils.setDocument(id,body);
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
	public String deleteDocument(String id) throws RmesException {
		// TODO Auto-generated method stub
		return null;
	}

}
