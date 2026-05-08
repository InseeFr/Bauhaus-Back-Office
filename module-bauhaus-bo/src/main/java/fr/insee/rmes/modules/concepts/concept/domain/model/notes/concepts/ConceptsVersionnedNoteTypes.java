package fr.insee.rmes.modules.concepts.concept.domain.model.notes.concepts;


import fr.insee.rmes.BauhausLanguagesProperties;
import jakarta.annotation.PostConstruct;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Énumération correspondant aux différents types de notes explicatives.
 */
public enum ConceptsVersionnedNoteTypes {
	SCOPENOTELG1("scopeNoteLg1") {
		@Override
		public String pathComponent() {return "definitionCourte";}
		@Override
		public String lang() {return languages.lg1();}
		@Override
		public IRI owlProperty() {return SKOS.SCOPE_NOTE;}

	},
	SCOPENOTELG2("scopeNoteLg2") {
		@Override
		public String pathComponent() {return "definitionCourte";}
		@Override
		public String lang() {return languages.lg2();}
		@Override
		public IRI owlProperty() {return SKOS.SCOPE_NOTE;}

	},
	DEFINITIONLG1("definitionLg1") {
		@Override
		public String pathComponent() {return "definition";}
		@Override
		public String lang() {return languages.lg1();}
		@Override
		public IRI owlProperty() {return SKOS.DEFINITION;}

	},
	DEFINITIONLG2("definitionLg2") {
		@Override
		public String pathComponent() {return "definition";}
		@Override
		public String lang() {return languages.lg2();}
		@Override
		public IRI owlProperty() {return SKOS.DEFINITION;}

	},
	EDITORIALNOTELG1("editorialNoteLg1") {
		@Override
		public String pathComponent() {return "noteEditoriale";}
		@Override
		public String lang() {return languages.lg1();}
		@Override
		public IRI owlProperty() {return SKOS.EDITORIAL_NOTE;}

	},
	EDITORIALNOTELG2("editorialNoteLg2") {
		@Override
		public String pathComponent() {return "noteEditoriale";}
		@Override
		public String lang() {return languages.lg2();}
		@Override
		public IRI owlProperty() {return SKOS.EDITORIAL_NOTE;}

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
        	 for (ConceptsVersionnedNoteTypes note : EnumSet.allOf(ConceptsVersionnedNoteTypes.class))
        		 note.setLanguages(languages);
        }
    }
	
    
	private static final Map<String, ConceptsVersionnedNoteTypes> map = new HashMap<>();
	
	private String text;

	ConceptsVersionnedNoteTypes(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return this.text;
	}
	
	public abstract String pathComponent();
	
	public abstract String lang();
	
	public abstract IRI owlProperty();
	
    public static ConceptsVersionnedNoteTypes getByName(String name) {
        return map.get(name);
    }

    static {
        for (ConceptsVersionnedNoteTypes c : ConceptsVersionnedNoteTypes.values()) {
            map.put(c.toString(), c);
        }
    }
	
}


