package fr.insee.rmes.persistance.service.sesame.links;

import fr.opensagres.xdocreport.core.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;

public class OperationsLink {

	@Schema(description = "Id of the resource linked", required = true)
	public String id;
	
	@Schema(description = "Type of object", required = true)
	public String type;

	@Schema(description = "Label lg1", required = true)
	public String labelLg1;

	@Schema(description = "Label lg2")
	public String labelLg2;

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getLabelLg1() {
		return labelLg1;
	}

	public String getLabelLg2() {
		return labelLg2;
	}

	public boolean isEmpty() {
		return StringUtils.isEmpty(id);
	}
}
