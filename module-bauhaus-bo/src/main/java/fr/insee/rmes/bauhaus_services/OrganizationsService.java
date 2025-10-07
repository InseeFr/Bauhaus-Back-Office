package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.model.organizations.Organization;

import java.util.List;

public interface OrganizationsService {

	IdLabelTwoLangs getOrganization(String organizationIdentifier) throws RmesException;

	String getOrganizationJsonString(String organizationUri) throws RmesException;
	
	String getOrganizationUriById(String organizationIdentifier) throws RmesException;

	String getOrganizationsJson() throws RmesException;
	
	List<Organization> getOrganizations() throws RmesException;

}
