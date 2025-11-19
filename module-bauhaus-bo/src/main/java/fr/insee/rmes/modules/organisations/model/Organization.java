package fr.insee.rmes.modules.organisations.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class Organization {
	
	private static final String classLink = "fr.insee.rmes.model.organizations.Organization";

	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;

	@Schema(description = "Identifier")
	public String identifier;

	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String labelLg1;

	@Schema(description = "Label lg2")
	public String labelLg2;

	@Schema(description = "Alternative value")
	public String altLabel;

	@Schema(description = "Uri of Type")
	public String type;

	@Schema(description = "Is part of")
	public String motherOrganization;

	@Schema(description = "Is linked to")
	public String linkedTo;

	@Schema(description = "See also")
	public String seeAlso;

	public static String getClassOperationsLink() {
		return classLink;
	}
}
