package fr.insee.rmes.persistance.service;

public interface OrganizationsService {


	String getOrganization(String organizationUri);

	String getOrganizations();

	String getOrganizationUriById(String organizationIdentifier);

}
