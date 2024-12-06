package fr.insee.rmes.bauhaus_services.rdf_utils;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RdfUtilsTest {
    @Test
    void shouldReturnNullWhenCallingAddTripeStringMdToXhtml2WithNullValue(){
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://iri");
        IRI predicate = SimpleValueFactory.getInstance().createIRI("http://predicate");
        String value = null;
        String lang = "fr";
        String prefix = "prefix";
        Model model = new LinkedHashModel();
        Resource graph = null;
        Assertions.assertNull(RdfUtils.addTripleStringMdToXhtml2(iri, predicate, value, lang, prefix, model, graph));
    }

    @Test
    void shouldReturnNullWhenCallingAddTripeStringMdToXhtml2WithEmptyValue(){
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://iri");
        IRI predicate = SimpleValueFactory.getInstance().createIRI("http://predicate");
        String value = "";
        String lang = "fr";
        String prefix = "prefix";
        Model model = new LinkedHashModel();
        Resource graph = null;
        Assertions.assertNull(RdfUtils.addTripleStringMdToXhtml2(iri, predicate, value, lang, prefix, model, graph));
    }

    @Test
    void shouldUpdateModelWhenCallingAddTripeStringMdToXhtml2WithValidValue(){
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://iri");
        IRI predicate = SimpleValueFactory.getInstance().createIRI("http://predicate");
        String value = "value";
        String lang = "fr";
        String prefix = "prefix";
        Model model = new LinkedHashModel();
        Resource graph = null;

        RdfUtils.addTripleStringMdToXhtml2(iri, predicate, value, lang, prefix, model, graph);
        Assertions.assertEquals("[(http://iri, http://predicate, http://iri/prefix/fr) [null], (http://iri/prefix/fr, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://rdf-vocabulary.ddialliance.org/xkos#ExplanatoryNote) [null], (http://iri/prefix/fr, http://eurovoc.europa.eu/schema#noteLiteral, \"<p>value</p>\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>) [null], (http://iri/prefix/fr, http://www.w3.org/2001/XMLSchema#language, \"fr\"^^<http://www.w3.org/2001/XMLSchema#language>) [null]]", model.toString());
    }
}