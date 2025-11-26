package fr.insee.rmes.modules.commons.configuration.swagger.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class IdLabelTwoLangs {
	
	private static final String classLink = "fr.insee.rmes.config.swagger.model.IdLabelTwoLangs";

	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;
	
	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String labelLg1;
	
	@Schema(description = "Label lg2")
	public String labelLg2;

	@Schema(description = "Creators")
	private List<String> creators;

	public IdLabelTwoLangs(String id, String labelLg1, String labelLg2) {
		super();
		this.id = id;
		this.labelLg1 = labelLg1;
		this.labelLg2 = labelLg2;
	}

	public IdLabelTwoLangs() {
		super();
	}
	
	public String getId() {
		return this.id;
	}

	public static String getClassIdLabelTwoLangs() {
		return classLink;
	}
	
	public String getLabelLg1() {
		return labelLg1;
	}

	public void setLabelLg1(String labelLg1) {
		this.labelLg1 = labelLg1;
	}

	public String getLabelLg2() {
		return labelLg2;
	}

	public void setLabelLg2(String labelLg2) {
		this.labelLg2 = labelLg2;
	}

	public void setId(String id) {
		this.id = id;
	}

    public void setCreators(List<String> creators) {
		this.creators = creators;
    }

	public List<String> getCreators() {
		return creators;
	}
}
