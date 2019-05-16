package fr.insee.rmes.persistance.service;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.json.JSONObject;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;


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
	public String setDocument(String body, InputStream documentFile) throws RmesException ;
	
	/*
	 * Update
	 */
	public String setDocument(String id, String body) throws RmesException ;
	
	/*
	 * Delete
	 */
	public String deleteDocument(String id) throws RmesException ;

}
