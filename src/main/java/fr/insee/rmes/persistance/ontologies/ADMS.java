package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class ADMS {
    private static final ValueFactory factory = SimpleValueFactory.getInstance();

    public static final String NAMESPACE = "http://www.w3.org/ns/adms#";

    public static final String PREFIX = "adms";

    private static IRI createIRI(String suffix) {
        return factory.createIRI(NAMESPACE, suffix);
    }

    public static final IRI IDENTIFIER = ADMS.createIRI("Identifier");
    public static final IRI HAS_IDENTIFIER = ADMS.createIRI("identifier");
}
