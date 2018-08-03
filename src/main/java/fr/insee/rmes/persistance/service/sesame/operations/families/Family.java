package fr.insee.rmes.persistance.service.sesame.operations.families;

import java.util.List;

import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import io.swagger.annotations.ApiModelProperty;

public class Family {

	@ApiModelProperty(value = "Id", required = true)
	public String id;

	@ApiModelProperty(value = "Label lg1", required = true)
	public String prefLabelLg1;

	@ApiModelProperty(value = "Label lg2")
	public String prefLabelLg2;


	@ApiModelProperty(value = "Abstract lg1, description")
	public String abstractLg1;


	@ApiModelProperty(value = "Abstract lg2")
	public String abstractLg2;

	@ApiModelProperty(value = "Subjects, Topics")
	public List<IdLabelTwoLangs> subjects;

	@ApiModelProperty(value = "Series")
	public List<IdLabelTwoLangs> series;
	
	
	public Family(String id) {
		this.id=id;
	}


	public String getPrefLabelLg1() {
		return prefLabelLg1;
	}

	public String getId() {
		return id;
	}

	public String getPrefLabelLg2() {
		return prefLabelLg2;
	}

	public String getAbstractLg1() {
		return abstractLg1;
	}

	public String getAbstractLg2() {
		return abstractLg2;
	}



	
}
