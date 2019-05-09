package fr.insee.rmes.persistance.service.sesame.operations.documentations.documents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	@Override
	public String getDocuments() throws RmesException {
		logger.info("Starting to get documents list");
		String resQuery = documentsUtils.getAllDocuments().toString();
		return resQuery;
	}
		
	/*
	 * Create
	 * @see fr.insee.rmes.persistance.service.DocumentsService#setDocument(java.lang.String)
	 */
	@Override
	public String setDocument(String body) throws RmesException {
		String id=documentsUtils.createDocumentID();
		documentsUtils.setDocument(id,body);
		return id;
	}
	
	/*
	 * Update
	 * @see fr.insee.rmes.persistance.service.DocumentsService#setDocument(java.lang.String)
	 */
	@Override
	public String setDocument(String id, String body) throws RmesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String deleteDocument(String id) throws RmesException {
		// TODO Auto-generated method stub
		return null;
	}

}
