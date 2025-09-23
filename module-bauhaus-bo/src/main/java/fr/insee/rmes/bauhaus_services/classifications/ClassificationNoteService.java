package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public interface ClassificationNoteService {
    void addNotes(Resource graph, String iri, String value, Model model) throws RmesException;
}
