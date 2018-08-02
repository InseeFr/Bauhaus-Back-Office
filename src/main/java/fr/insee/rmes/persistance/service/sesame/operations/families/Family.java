package fr.insee.rmes.persistance.service.sesame.operations.families;

public class Family {

	private String id;
	private String prefLabelLg1;
	private String prefLabelLg2;
	
	//TODO topics
	
	private String abstractLg1;
	private String abstractLg2;
	
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
