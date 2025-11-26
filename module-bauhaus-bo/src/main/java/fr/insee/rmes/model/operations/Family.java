package fr.insee.rmes.model.operations;

import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class Family {

	@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
	public String id;

	@Schema(description = "Label lg1", requiredMode = Schema.RequiredMode.REQUIRED)
	public String prefLabelLg1;

	@Schema(description = "Label lg2")
	public String prefLabelLg2;


	@Schema(description = "Abstract lg1, description")
	public String abstractLg1;


	@Schema(description = "Abstract lg2")
	public String abstractLg2;

	@Schema(description = "Subjects, Topics")
	public List<IdLabelTwoLangs> subjects;

	@Schema(description = "Series")
	public List<IdLabelTwoLangs> series;

	@Schema(description =  "Creation date")
	private String created;

	@Schema(description =  "Update date")
	private String updated;


	public Family() {
		//empty constructor for Jackson mapper
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

	public void setAbstractLg1(String abstractLg1) {
		this.abstractLg1 = abstractLg1;
	}

	public void setAbstractLg2(String abstractLg2) {
		this.abstractLg2 = abstractLg2;
	}

	public String getAbstractLg2() {
		return abstractLg2;
	}

	public void setId(String id) {
		this.id = id;
	}


    public void setCreated(String currentDate) {
		this.created = currentDate;
    }

	public void setUpdated(String currentDate) {
		this.updated = currentDate;
	}

	public String getUpdated() {
		return updated;
	}

	public String getCreated() {
		return created;
	}
}
