package fr.insee.rmes.persistance.service;

import fr.insee.rmes.exceptions.RmesException;

public interface OrganizationsService {


	String getOrganization(String organizationUri) throws RmesException;

	String getOrganizations() throws RmesException;

	String getOrganizationUriById(String organizationIdentifier) throws RmesException;

}
