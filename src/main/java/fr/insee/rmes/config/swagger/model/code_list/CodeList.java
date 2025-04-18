package fr.insee.rmes.config.swagger.model.code_list;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeList {

	@Schema(description="Code list notation")
	public String notation;

	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String labelLg1;

	@Schema(description = "Label lg2")
	public String labelLg2;

	@Schema(description = "List of codes")
	public List<CodeLabelTwoLangs> codes;

	public String range;

	public String uri;

	public String id;

	public String iri;

	public String creator;

	public List<String> contributor;

	public String created;

	public String lastListUriSegment;

	public String modified;

	public String lastClassUriSegment;

	public String disseminationStatus;

	public String validationState;

	public String descriptionLg1;
	public String descriptionLg2;

	public String lastCodeUriSegment;


	public CodeList(String notation) {
		this.notation = notation;
	}

	public CodeList() {
	}

	public String getNotation() {
		return notation;
	}

	public String getIri() {
		return iri;
	}


}
