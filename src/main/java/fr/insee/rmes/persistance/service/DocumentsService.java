package fr.insee.rmes.persistance.service;


import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import fr.insee.rmes.exceptions.RmesException;


public interface DocumentsService {

	/*
	 * Read one document
	 */

	public JSONObject getDocument(String id) throws RmesException ;
	
	/*
	 * Read all documents
	 */
	public String getDocuments() throws RmesException ;
	
	/*
	 * Create
	 */
	public String setDocument(String body, InputStream documentFile, String documentName) throws RmesException ;
	
	/*
	 * Update
	 */
	public String setDocument(String id, String body) throws RmesException ;
	
	/*
	 * Delete
	 * Delete the document and the links towards it.
	 */
	public Status deleteDocument(String id) throws RmesException ;

	/*
	 * Change an uploaded document
	 * Delete the previous document
	 * Update the document's url if the document's name has changed
	 */
	public String changeDocument(String documentUri, InputStream documentFile, String documentName) throws RmesException ;

	/*
	 * LINKS
	 */
	
	/*
	 * Create new link
	 */
	public String setLink(String body) throws RmesException;

	public Response downloadDocument(String id) throws RmesException, IOException;

}
