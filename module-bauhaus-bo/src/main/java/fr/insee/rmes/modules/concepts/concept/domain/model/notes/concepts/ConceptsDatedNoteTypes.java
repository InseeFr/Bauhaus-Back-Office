package fr.insee.rmes.modules.concepts.concept.domain.model.notes.concepts;

import fr.insee.rmes.BauhausLanguagesProperties;
import jakarta.annotation.PostConstruct;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.stereotype.Component;

import java.util.EnumSet;


public enum ConceptsDatedNoteTypes {
	
	CHANGENOTELG1("changeNoteLg1") {
		@Override
		public String pathComponent() {return "changeNote";}
		@Override
		public String lang() {return languages.lg1();}
		@Override
		public IRI owlProperty() {return SKOS.CHANGE_NOTE;}

	},
	CHANGENOTELG2("changeNoteLg2") {
		@Override
		public String pathComponent() {return "changeNote";}
		@Override
		public String lang() {return languages.lg2();}
		@Override
		public IRI owlProperty() {return SKOS.CHANGE_NOTE;}

	};

	private static BauhausLanguagesProperties languages;

	protected void setLanguages(BauhausLanguagesProperties languagesParam) {
		languages = languagesParam;
	}


    @Component
    public static class ConfigServiceInjector {
        private final BauhausLanguagesProperties languages;

        public ConfigServiceInjector(BauhausLanguagesProperties languages) {
            this.languages = languages;
        }

        @PostConstruct
        public void postConstruct() {
        	 for (ConceptsDatedNoteTypes note : EnumSet.allOf(ConceptsDatedNoteTypes.class))
        		 note.setLanguages(languages);
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
