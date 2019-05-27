package fr.insee.rmes.persistance.service;


import java.io.InputStream;

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
	 */
	public Status deleteDocument(String id) throws RmesException ;

	/*
	 * Change an uploaded document
	 */
	public String changeDocument(String documentUri, InputStream documentFile, String documentName) throws RmesException ;

}
