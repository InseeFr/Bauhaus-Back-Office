package fr.insee.rmes.modules.commons.configuration.swagger.model.code_list;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeList {

	public String notation;

	public String labelLg1;

	public String labelLg2;

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
