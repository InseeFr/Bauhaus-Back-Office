package fr.insee.rmes.bauhaus_services.code_list;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeListItem {

	private static final String classLink = "fr.insee.rmes.bauhaus_services.code_list.CodeListItem";

	@Schema(description = "Iri", requiredMode = Schema.RequiredMode.REQUIRED)
	private String iri;

	@Schema(description = "Code", requiredMode = Schema.RequiredMode.REQUIRED)
	private String code;
	
	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	private String labelLg1;
	
	@Schema(description = "Label lg2")
	private String labelLg2;

	private String descriptionLg1;
	private String descriptionLg2;
	private String lastCodeUriSegment;
	private String codeUri;
	public List<String> broader;

	public CodeListItem(String code, String labelLg1, String labelLg2, String iri) {
		super();
		this.code = code;
		this.labelLg1 = labelLg1;
		this.labelLg2 = labelLg2;
		this.iri = iri;
	}

	public CodeListItem(String code) {
		this.code = code;
	}

	public CodeListItem() {
		super();
	}
	
	public String getCode() {
		return this.code;
	}

	public List<String> getBroader() {
		return this.broader;
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

	public void setCode(String code) {
		this.code = code;
	}

	public String getIri() {
		return iri;
	}

	public void setIri(String iri) {
		this.iri = iri;
	}

	public String getDescriptionLg1() {
		return descriptionLg1;
	}

	public String getDescriptionLg2() {
		return descriptionLg2;
	}

	public String getLastCodeUriSegment() {
		return lastCodeUriSegment;
	}

	public String getCodeUri() {
		return codeUri;
	}

	public static String getClassOperationsLink() {
		return classLink;

	}
}