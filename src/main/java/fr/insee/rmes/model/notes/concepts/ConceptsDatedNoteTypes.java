package fr.insee.rmes.model.notes.concepts;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.rmes.config.Config;


public enum ConceptsDatedNoteTypes {
	
	CHANGENOTELG1("changeNoteLg1") {
		@Override
		public String pathComponent() {return "changeNote";}
		@Override
		public String lang() {return config.getLg1();}
		@Override
		public IRI owlProperty() {return SKOS.CHANGE_NOTE;}

	},
	CHANGENOTELG2("changeNoteLg2") {
		@Override
		public String pathComponent() {return "changeNote";}
		@Override
		public String lang() {return config.getLg2();}
		@Override
		public IRI owlProperty() {return SKOS.CHANGE_NOTE;}

	};

	@Autowired 
	static Config config;
	
	private String text;

	ConceptsDatedNoteTypes(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return this.text;
	}
	
	public abstract String pathComponent();
	
	public abstract String lang();
		
	public abstract IRI owlProperty();
}
