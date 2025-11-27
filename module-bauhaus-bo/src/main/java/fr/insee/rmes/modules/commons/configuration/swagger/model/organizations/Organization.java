package fr.insee.rmes.modules.commons.configuration.swagger.model.organizations;

import io.swagger.v3.oas.annotations.media.Schema;

public class Organization {

	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;

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
}
