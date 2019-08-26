package fr.insee.rmes.persistance.service;

import fr.insee.rmes.exceptions.RmesException;

public interface DSDService {
	
	public String getDSDs() throws RmesException;
	
	public String getDSDById(String id) throws RmesException;
	
	public String getDSDComponents(String dsdId) throws RmesException;
	
	public String getDSDDetailedComponents(String dsdId) throws RmesException;
	
	public String getDSDComponentById(String dsdId, String componentId) throws RmesException;
	
	public String setDSD(String body) throws RmesException;
	
	public String setDSD(String id, String body) throws RmesException;
	
}
