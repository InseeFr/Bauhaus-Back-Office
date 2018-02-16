package fr.insee.rmes.persistance.service.sesame.notes.concepts;


import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.SKOS;

import fr.insee.rmes.config.Config;

/**
 * Énumération correspondant aux différents types de notes explicatives.
 */
public enum ConceptsVersionnedNoteTypes {
	SCOPENOTELG1("scopeNoteLg1") {
		@Override
		public String pathComponent() {return "definitionCourte";}
		@Override
		public String lang() {return Config.LG1;}
		@Override
		public URI owlProperty() {return SKOS.SCOPE_NOTE;}

	},
	SCOPENOTELG2("scopeNoteLg2") {
		@Override
		public String pathComponent() {return "definitionCourte";}
		@Override
		public String lang() {return Config.LG2;}
		@Override
		public URI owlProperty() {return SKOS.SCOPE_NOTE;}

	},
	DEFINITIONLG1("definitionLg1") {
		@Override
		public String pathComponent() {return "definition";}
		@Override
		public String lang() {return Config.LG1;}
		@Override
		public URI owlProperty() {return SKOS.DEFINITION;}

	},
	DEFINITIONLG2("definitionLg2") {
		@Override
		public String pathComponent() {return "definition";}
		@Override
		public String lang() {return Config.LG2;}
		@Override
		public URI owlProperty() {return SKOS.DEFINITION;}

	},
	EDITORIALNOTELG1("editorialNoteLg1") {
		@Override
		public String pathComponent() {return "noteEditoriale";}
		@Override
		public String lang() {return Config.LG1;}
		@Override
		public URI owlProperty() {return SKOS.EDITORIAL_NOTE;}

	},
	EDITORIALNOTELG2("editorialNoteLg2") {
		@Override
		public String pathComponent() {return "noteEditoriale";}
		@Override
		public String lang() {return Config.LG2;}
		@Override
		public URI owlProperty() {return SKOS.EDITORIAL_NOTE;}

	};
		
	private static final Map<String, ConceptsVersionnedNoteTypes> map = new HashMap<String, ConceptsVersionnedNoteTypes>();
	
	private String text;

	ConceptsVersionnedNoteTypes(String text) {
		this.text = text;
	}

	public String toString() {
		return this.text;
	}
	
	public abstract String pathComponent();
	
	public abstract String lang();
	
	public abstract URI owlProperty();
	
    public static ConceptsVersionnedNoteTypes getByName(String name) {
        return map.get(name);
    }

    static {
        for (ConceptsVersionnedNoteTypes c : ConceptsVersionnedNoteTypes.values()) {
            map.put(c.toString(), c);
        }
    }
	
}


