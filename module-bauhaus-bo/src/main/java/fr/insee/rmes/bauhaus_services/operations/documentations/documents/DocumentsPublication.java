package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.FilesOperations;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DocumentsPublication  extends RdfService{

	private final DocumentsUtils docUtils;

    private final FilesOperations filesOperations;

	static final Logger logger = LoggerFactory.getLogger(DocumentsPublication.class);

    public DocumentsPublication(DocumentsUtils docUtils, FilesOperations filesOperations) {
        this.docUtils = docUtils;
        this.filesOperations = filesOperations;
    }

    public void publishAllDocumentsInSims(String idSims) throws RmesException {
		
		// Get all documents
		JSONArray listDoc = docUtils.getListDocumentSims(idSims);
		
		Map<Integer,String> mapIdUrls = new HashMap<>();
		listDoc.forEach(doc -> mapIdUrls.put(docUtils.getIdFromJson((JSONObject) doc), DocumentsUtils.getDocumentUrlFromDocument((JSONObject) doc)));

		for (Map.Entry<Integer, String> doc : mapIdUrls.entrySet()) {
			String docId = doc.getKey().toString();
			String originalPath = doc.getValue();
			String filename = DocumentsUtils.getDocumentNameFromUrl(originalPath);
			// Publish the physical files
			copyFileInPublicationFolders(originalPath);
			// Change url in document (getModelToPublish) and publish the RDF
			Resource document = RdfUtils.objectIRIPublication(ObjectType.DOCUMENT,docId);
			repositoryPublication.publishResource(document, getModelToPublish(docId,filename), ObjectType.DOCUMENT.labelType());
		}
		
		//Get all links
		JSONArray listLinks = docUtils.getListLinksSims(idSims);
		for (Object link : listLinks) {
			String id = docUtils.getIdFromJson((JSONObject)link).toString();
			Resource linkResource = RdfUtils.objectIRIPublication(ObjectType.LINK,id);
			repositoryPublication.publishResource(linkResource, getLinkModelToPublish(id), ObjectType.LINK.labelType());
		}

	}

	private void copyFileInPublicationFolders(String originalPath){
        String documentsStoragePublicationExterne = config.getDocumentsStoragePublicationExterne();
        filesOperations.copyFromGestionToPublication(originalPath, documentsStoragePublicationExterne);
	}




	private Model getModelToPublish(String documentId, String filename) throws RmesException {
		Model model = new LinkedHashModel();
		Resource document = RdfUtils.documentIRI(documentId);

        RepositoryResult<Statement> documentStatements=null;

        try (RepositoryConnection con = repoGestion.getConnection()) {
            documentStatements = repoGestion.getStatements(con, document);

            if (!documentStatements.hasNext()) {
                throw new RmesNotFoundException(ErrorCodes.DOCUMENT_UNKNOWN_ID, "Document not found", documentId);
            }
            while (documentStatements.hasNext()) {
                Statement st = documentStatements.next();
                if (RdfUtils.toString(st.getPredicate()).endsWith(Constants.URL)) {
                    Resource subject = publicationUtils.tranformBaseURIToPublish(st.getSubject());
                    IRI predicate = RdfUtils
                            .createIRI(publicationUtils.tranformBaseURIToPublish(st.getPredicate()).stringValue());
                    String newUrl = config.getDocumentsBaseurl() + "/" + filename;
                    logger.info("Publishing document : {}", newUrl);
                    Value object = RdfUtils.toURI(newUrl);
                    model.add(subject, predicate, object, st.getContext());
                } else {
                    Resource subject = publicationUtils.tranformBaseURIToPublish(st.getSubject());
                    renameAndAddTripleToModel(model, st, subject);
                }
            }
        } catch (RepositoryException | RmesException e) {
            model = getModelWithErrorToPublish(documentId, filename);
        } finally {
            if (documentStatements != null) {
                repoGestion.closeStatements(documentStatements);
            }
        }
		return model;
	}
	
	private Model getModelWithErrorToPublish(String documentId, String filename) throws RmesException {
		logger.error("PUBLISHING A DOCUMENT WITH RDF ERROR (URL)");
		Model model = new LinkedHashModel();
		Resource document = RdfUtils.documentIRI(documentId);
		
		try {
			JSONArray tuples = repoGestion.getResponseAsArray(
                    "select ?predicat ?obj FROM <"+RdfUtils.documentsGraph()+"> "
					+ "WHERE {"
					+ "?document ?predicat ?obj . "
					+ "FILTER (?document = <"+document+">) "
					+ "}");

			if (tuples.isEmpty()) {
				throw new RmesNotFoundException(ErrorCodes.DOCUMENT_UNKNOWN_ID, "Document not found", documentId);
			}
			
			transformTuplesToPublish(filename, model, document, tuples);
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					Constants.REPOSITORY_EXCEPTION);
		}
		return model;
		
	}

	private void transformTuplesToPublish(String filename, Model model, Resource document, JSONArray tuples) {
		Resource newSubject = publicationUtils.tranformBaseURIToPublish(document);
		Value object ;
		
		for (int i = 0; i < tuples.length(); i++) {
			JSONObject tuple = (JSONObject) tuples.get(i);
			String predicatString = tuple.getString("predicat");
			IRI predicate = (SimpleIRI) publicationUtils.tranformBaseURIToPublish(RdfUtils.toURI(predicatString));			
			if (predicatString.endsWith(Constants.URL)) {
				String newUrl = config.getDocumentsBaseurl() + "/"+ filename;
				logger.info("Publishing document : {}",newUrl);
				object = RdfUtils.toURI(newUrl);
			} else {
				String objectString = tuple.getString("obj");
				try {					
					object = RdfUtils.toURI(objectString);
					object = publicationUtils.tranformBaseURIToPublish((Resource) object);

				}catch(IllegalArgumentException iAe) {
					object = RdfUtils.setLiteralString(objectString);
				}
			}
			model.add(newSubject, predicate, object, RdfUtils.documentsGraph());
		}
	}
	
	private Model getLinkModelToPublish(String linkId) throws RmesException {
		Model model = new LinkedHashModel();
		Resource link = RdfUtils.linkIRI(linkId);

		RepositoryConnection con = repoGestion.getConnection();
        RepositoryResult<Statement> linkStatements =null;

        try (con) {
            linkStatements=repoGestion.getStatements(con, link);
            if (!linkStatements.hasNext()) {
                throw new RmesNotFoundException(ErrorCodes.LINK_UNKNOWN_ID, "Link not found", linkId);
            }
            while (linkStatements.hasNext()) {
                Statement st = linkStatements.next();
                Resource subject = publicationUtils.tranformBaseURIToPublish(st.getSubject());
                renameAndAddTripleToModel(model, st, subject);

            }
        } catch (RepositoryException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
                    Constants.REPOSITORY_EXCEPTION);
        } finally {
            if (linkStatements != null) {
                repoGestion.closeStatements(linkStatements);
            }
        }
		return model;
	}

	public void renameAndAddTripleToModel(Model model, Statement st, Resource subject) {
		IRI predicate = RdfUtils
				.createIRI(publicationUtils.tranformBaseURIToPublish(st.getPredicate()).stringValue());
		Value object = st.getObject();
		if (st.getObject() instanceof Resource resource) {
			object = publicationUtils.tranformBaseURIToPublish(resource);
		}
		model.add(subject, predicate, object, st.getContext());
	}

}