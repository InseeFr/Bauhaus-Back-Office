package fr.insee.rmes.config.swagger.model.dsd;

import io.swagger.v3.oas.annotations.media.Schema;

public class DSDComponentById {
	
	@Schema(description = "Label lg1", required = true)
	public String labelLg1;
	
	@Schema(description = "Label lg2", required = false)
	public String labelLg2;
	
	@Schema(description = "Concept label lg1", required = true)
	public String conceptLabelLg1;
	
	@Schema(description = "Concept label lg2", required = false)
	public String conceptLabelLg2;
	
	@Schema(description = "Range label lg1", required = true)
	public String rangeLabelLg1;
	
	@Schema(description = "Range label lg2", required = false)
	public String rangeLabelLg2;
	
	@Schema(description = "Code list label lg1", required = false)
	public String codeListLabelLg1;
	
	@Schema(description = "Code list label lg2", required = false)
	public String codeListLabelLg2;
	
	@Schema(description = "Attachment URI", required = false)
	public String attachment;

}
