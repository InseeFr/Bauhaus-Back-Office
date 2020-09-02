package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.exceptions.RmesException;

public interface OrganizationsService {

	IdLabelTwoLangs getOrganization(String organizationIdentifier) throws RmesException;

	String getOrganizationJsonString(String organizationUri) throws RmesException;

	String getOrganizationsJson() throws RmesException;
	
	String getOrganizationUriById(String organizationIdentifier) throws RmesException;

}
