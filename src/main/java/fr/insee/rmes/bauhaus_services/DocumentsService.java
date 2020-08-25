package fr.insee.rmes.bauhaus_services;


import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import fr.insee.rmes.exceptions.RmesException;


public interface DocumentsService {

	/**
	 * Read one document
	 */
	public JSONObject getDocument(String id) throws RmesException ;
	
	/**
	 * Read all documents
	 */
	public String getDocuments() throws RmesException ;
	
	/**
	 * Create a document
	 */
	public String setDocument(String body, InputStream documentFile, String documentName) throws RmesException ;
	
	/**
	 * Update
	 */
	public String setDocument(String id, String body) throws RmesException ;
	
	/**
	 * Delete
	 * Delete the document and the links towards it.
	 */
	public Status deleteDocument(String id) throws RmesException ;

	/**
	 * Change an uploaded document file
	 * Delete the previous document file
	 * Update the document's url if the document's name has changed
	 */
	public String changeDocument(String documentUri, InputStream documentFile, String documentName) throws RmesException ;
	
	public Response downloadDocument(String id) throws RmesException, IOException;


	/*
	 * LINKS
	 */
	
	/**
	 * Create new link
	 */
	public String setLink(String body) throws RmesException;

	public Object getLink(String id) throws RmesException;

	public Status deleteLink(String id) throws RmesException;

	/**
	 * update
	 * @param id
	 * @param body
	 * @return 
	 */
	public String setLink(String id, String body) throws RmesException;

}
