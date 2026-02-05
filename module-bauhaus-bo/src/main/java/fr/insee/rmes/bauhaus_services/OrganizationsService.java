package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.model.Organization;

import java.util.List;

public interface OrganizationsService {

	IdLabelTwoLangs getOrganization(String organizationIdentifier) throws RmesException;

	String getOrganizationJsonString(String organizationUri) throws RmesException;
	
	String getOrganizationUriById(String organizationIdentifier) throws RmesException;

	String getOrganizationsJson() throws RmesException;
	
	List<Organization> getOrganizations() throws RmesException;

}
