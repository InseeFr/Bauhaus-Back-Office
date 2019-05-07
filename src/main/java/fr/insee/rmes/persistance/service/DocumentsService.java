package fr.insee.rmes.persistance.service;

import javax.ws.rs.core.Response;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;


public interface DocumentsService {

	/*
	 * Read all documents
	 */
	public String getDocuments() throws RmesException ;
	
	/*
	 * Create
	 */
	public String setDocument(String body) throws RmesException ;
	
	/*
	 * Update
	 */
	public String setDocument(String id, String body) throws RmesException ;
	
	/*
	 * Delete
	 */
	public String deleteDocument(String id) throws RmesException ;
}
