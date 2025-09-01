package fr.insee.rmes.bauhaus_services;


import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;


public interface DocumentsService {

	/**
	 * Read one document
	 */
	JSONObject getDocument(String id) throws RmesException ;
	
	/**
	 * Read all documents
	 */
	String getDocuments() throws RmesException ;
	
	/**
	 * Create a document
	 */
	String createDocument(String body, InputStream documentFile, String documentName) throws RmesException, IOException;
	
	/**
	 * Update
	 */
	void setDocument(String id, String body) throws RmesException ;
	
	/**
	 * Delete
	 * Delete the document and the links towards it.
	 */
	HttpStatus deleteDocument(String id) throws RmesException ;

	/**
	 * Change an uploaded document file
	 * Delete the previous document file
	 * Update the document's url if the document's name has changed
	 */
	String changeDocument(String documentUri, InputStream documentFile, String documentName) throws RmesException ;
	
	ResponseEntity<Resource> downloadDocument(String id) throws RmesException;


	/*
	 * LINKS
	 */
	
	String setLink(String body) throws RmesException, IOException;

	JSONObject getLink(String id) throws RmesException;

	HttpStatus deleteLink(String id) throws RmesException;

	String setLink(String id, String body) throws RmesException;

}
