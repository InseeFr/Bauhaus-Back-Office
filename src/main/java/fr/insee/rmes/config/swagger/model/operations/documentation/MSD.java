package fr.insee.rmes.config.swagger.model.operations.documentation;

import io.swagger.v3.oas.annotations.media.Schema;

public class MSD {

	@Schema(description="Id of the Metadata Attribute Specification")
	public String idMas;
	public String masLabelLg1;
	public String masLabelLg2;
	public String idParent;
	public Boolean isPresentational;
}
