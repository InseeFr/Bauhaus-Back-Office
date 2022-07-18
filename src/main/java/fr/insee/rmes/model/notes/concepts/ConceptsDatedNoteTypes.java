package fr.insee.rmes.model.notes.concepts;

import java.util.EnumSet;

import javax.annotation.PostConstruct;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

	private static Config config;
	
	protected void setConfig(Config configParam) {
		config = configParam;
	}
	

    @Component
    public static class ConfigServiceInjector {
        @Autowired
        private Config config;

        @PostConstruct
        public void postConstruct() {
        	 for (ConceptsDatedNoteTypes note : EnumSet.allOf(ConceptsDatedNoteTypes.class))
        		 note.setConfig(config);
        }
    }
	
    
	
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
