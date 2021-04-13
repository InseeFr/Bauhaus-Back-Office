package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;

@Component
public class DocumentsPublication  extends RdfService{

	@Autowired
	DocumentsUtils docUtils;
	

	static final Logger logger = LogManager.getLogger(DocumentsPublication.class);

	public void publishAllDocumentsInSims(String idSims, Model model) throws RmesException {
		// Get all documents
		JSONArray listDoc = docUtils.getListDocumentSims(idSims);
		
		Map<Integer,String> mapIdUrls = new HashMap<>();
		listDoc.forEach(doc -> mapIdUrls.put(docUtils.getIdFromJson((JSONObject) doc), docUtils.getDocumentUrlFromDocument((JSONObject) doc)));

		for (Map.Entry<Integer, String> doc : mapIdUrls.entrySet()) {
			String docId = doc.getKey().toString();
			String originalPath = doc.getValue();
			String filename = docUtils.getDocumentNameFromUrl(originalPath);
			// Publish the physical files
			copyFileInPublicationFolders(originalPath);
			
			// Change url in document
			model.addAll(getModelToPublish(docId,filename));
		}
		
		//Get all links
		JSONArray listLinks = docUtils.getListLinksSims(idSims);
		for (Object link : listLinks) {
			model.addAll(getLinkModelToPublish(docUtils.getIdFromJson((JSONObject)link).toString()));
		}		
		
	}

	private void copyFileInPublicationFolders(String originalPath) throws RmesException {
		Path file = Paths.get(originalPath);
		Path targetPathInt = Paths.get(Config.DOCUMENTS_STORAGE_PUBLICATION_INTERNE);
		Path targetPathExt = Paths.get(Config.DOCUMENTS_STORAGE_PUBLICATION_EXTERNE);

		try {
			Files.copy(file, targetPathInt.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(file, targetPathExt.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getClass() + e.getMessage(),
					e.getClass() + " - Can't copy files");
		}
	}
	
	private Model getModelToPublish(String documentId, String filename) throws RmesException {
		Model model = new LinkedHashModel();
		Resource document = RdfUtils.documentIRI(documentId);

		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> documentStatements = repoGestion.getStatements(con, document);

		try {
			if (!documentStatements.hasNext()) {
				throw new RmesNotFoundException(ErrorCodes.DOCUMENT_UNKNOWN_ID, "Document not found", documentId);
			}
			while (documentStatements.hasNext()) {
				Statement st = documentStatements.next();
				if (((SimpleIRI)st.getPredicate()).toString().endsWith("url")) {
					Resource subject = PublicationUtils.tranformBaseURIToPublish(st.getSubject());
					IRI predicate = RdfUtils
							.createIRI(PublicationUtils.tranformBaseURIToPublish(st.getPredicate()).stringValue());
					Value object = RdfUtils.toURI(Paths.get(Config.DOCUMENTS_BASEURL,filename).toString());
					model.add(subject, predicate, object, st.getContext());
				} else {
					Resource subject = PublicationUtils.tranformBaseURIToPublish(st.getSubject());
					renameAndAddTripleToModel(model, st, subject);
				}
			}
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					Constants.REPOSITORY_EXCEPTION);
		}

		finally {
			repoGestion.closeStatements(documentStatements);
		}
		return model;
	}
	
	private Model getLinkModelToPublish(String linkId) throws RmesException {
		Model model = new LinkedHashModel();
		Resource link = RdfUtils.linkIRI(linkId);

		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> linkStatements = repoGestion.getStatements(con, link);

		try {
			if (!linkStatements.hasNext()) {
				throw new RmesNotFoundException(ErrorCodes.LINK_UNKNOWN_ID, "Link not found", linkId);
			}
			while (linkStatements.hasNext()) {
				Statement st = linkStatements.next();
					Resource subject = PublicationUtils.tranformBaseURIToPublish(st.getSubject());
					renameAndAddTripleToModel(model, st, subject);
				
			}
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					Constants.REPOSITORY_EXCEPTION);
		}

		finally {
			repoGestion.closeStatements(linkStatements);
		}
		return model;
	}

	public void renameAndAddTripleToModel(Model model, Statement st, Resource subject) {
		IRI predicate = RdfUtils
				.createIRI(PublicationUtils.tranformBaseURIToPublish(st.getPredicate()).stringValue());
		Value object = st.getObject();
		if (st.getObject() instanceof Resource) {
			object = PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject());
		}
		model.add(subject, predicate, object, st.getContext());
	}

}