package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.graphdb.ontologies.INSEE;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

/** Lecture du validationState écrit dans un modèle RDF, partagée par les tests des dépôts legacy. */
public final class ValidationStateModels {

    private ValidationStateModels() {}

    public static String validationStateOf(Model model) {
        for (Statement st : model) {
            if (st.getPredicate().equals(INSEE.VALIDATION_STATE)) {
                return st.getObject().stringValue();
            }
        }
        return null;
    }
}
