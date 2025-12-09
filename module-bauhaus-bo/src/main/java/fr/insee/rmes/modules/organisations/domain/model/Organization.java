package fr.insee.rmes.modules.organisations.domain.model;


public class Organization {
	
	private static final String classLink = "fr.insee.rmes.model.organizations.Organization";

	public String id;

	public String identifier;

	public String labelLg1;

	public String labelLg2;

	public String altLabel;

	public String type;

	public String motherOrganization;

	public String linkedTo;

	public String seeAlso;

	public static String getClassOperationsLink() {
		return classLink;
	}
}
