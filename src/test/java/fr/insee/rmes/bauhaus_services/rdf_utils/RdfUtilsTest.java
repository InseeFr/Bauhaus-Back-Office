package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.model.ValidationStatus;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.base.InternedIRI;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class RdfUtilsTest {

    Model model ;
    IRI predicate;
    Resource graph;
    Resource objectResource;
    IRI objectIri;
    IRI valueIri;

    @BeforeEach
   void setUp(){
       this.model=new LinkedHashModel();
       this.predicate=new InternedIRI("namespacePredicate","localNamePredicate");
       this.graph =new InternedIRI("namespaceGraph","localNameGraph");
       this.objectResource = new InternedIRI("namespaceObject","localNameObject");
       this.objectIri= new InternedIRI("https://namespaceObject","localNameObject");
       this.valueIri = new InternedIRI("namespaceValue","localNameValue");
   }

    @Test
    void shouldCreateXSDIRI() {
        IRI iriActual = RdfUtils.createXSDIRI("example");
        boolean iriActualIsExpected = "http://www.w3.org/2001/XMLSchema#example".equals(iriActual.toString());
        assertTrue(iriActualIsExpected);
    }

    @Test
    void shouldAddTripleNode() {
        BNode value = RdfUtils.createBlankNode();
        int modelSizeBefore = model.size();
        RdfUtils.addTripleBNode(objectResource,predicate,value,model,graph);
        int modelSizeAfter = model.size();
        assertTrue(modelSizeBefore<modelSizeAfter);
    }

    @Test
    void shouldAddTripleUri() {

        int modelSizeBefore = model.size();
        RdfUtils.addTripleUri(objectIri,predicate,valueIri,model,graph);
        int modelSizeAfter = model.size();

        boolean compareSizesModel = modelSizeBefore<modelSizeAfter;

        Resource objectOther = new InternedIRI("namespaceObject","localNameObject");
        Model modelOther = new LinkedHashModel();

        int modelOtherSizeBefore = modelOther.size();
        RdfUtils.addTripleUri(objectOther,predicate,"urn:example:example",modelOther,graph);
        int modelOtherSizeAfter =modelOther.size();

        boolean compareSizesModelOther = modelOtherSizeBefore<modelOtherSizeAfter;

        assertTrue(compareSizesModel && compareSizesModelOther);
    }

    @Test
    void shouldAddTripleInt() {
        int modelSizeBefore = model.size();
        RdfUtils.addTripleInt(objectIri,predicate,"urn:example:example",model,graph);
        int modelSizeAfter = model.size();
        assertTrue(modelSizeBefore<modelSizeAfter);
    }

    @Test
    void shouldAddTripleLiteralXML() {
        int modelSizeBefore = model.size();
        RdfUtils.addTripleLiteralXML(objectIri,predicate,"valueExample",model,graph);
        int modelSizeAfter = model.size();
        assertTrue(modelSizeBefore<modelSizeAfter);
    }

    @Test
    void shouldAddTripleDate() {
        int modelSizeBefore = model.size();
        RdfUtils. addTripleDate(objectIri,predicate,"2025-04-08",model,graph);
        int modelSizeAfter = model.size();
        assertTrue(modelSizeBefore<modelSizeAfter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025-04-08", "2011-12-03T10:15:30Z" })
    void shouldAddTripleDateTime(String value) {
        int modelSizeBefore = model.size();
        RdfUtils.addTripleDateTime(objectIri,predicate,value,model,graph);
        int modelSizeAfter = model.size();
        assertTrue(modelSizeBefore<modelSizeAfter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"fr","example","2025" })
    void shouldAddTripleLanguage(String value) {
        int modelSizeBefore = model.size();
        RdfUtils.addTripleLanguage(objectIri,predicate,value,model,graph);
        int modelSizeAfter = model.size();
        assertTrue(modelSizeBefore<modelSizeAfter);
    }

    @Test
    void shouldAddTripleStringMdToXhtml2() {
        IRI resultFirst = RdfUtils. addTripleStringMdToXhtml2(objectIri, predicate,"https://", "lang", "prefix", model, graph);
        IRI resultSecond = RdfUtils. addTripleStringMdToXhtml2(objectIri, predicate,"", "lang", "prefix", model, graph);
        boolean createUri = "https://namespaceObjectlocalNameObject/prefix/lang".equals(resultFirst.toString());
        boolean notCreateUri = resultSecond==null;
        assertTrue(createUri && notCreateUri);
    }

    @Test
    void shouldAddTripleStringMdToXhtml() {
        int modelSizeBefore = model.size();
        RdfUtils.addTripleStringMdToXhtml(objectIri,predicate,"example","fr",model,graph);
        int modelSizeAfter = model.size();
        assertTrue(modelSizeBefore<modelSizeAfter);
    }

    @Test
    void shouldAddTripleString() {

        int modelSizeBefore = model.size();
        RdfUtils.addTripleString(objectIri,predicate,"example","fr",model,graph);
        int modelSizeAfter = model.size();
        boolean compareSizesModel = modelSizeBefore<modelSizeAfter;

        setUp();

        Model modelOther = new LinkedHashModel();
        int modelOtherSizeBefore = modelOther.size();
        RdfUtils.addTripleString(objectIri,predicate,"example",modelOther,graph);
        int modelOtherSizeAfter = modelOther.size();
        boolean compareOtherSizesModel = modelOtherSizeBefore<modelOtherSizeAfter;

        assertTrue(compareSizesModel && compareOtherSizesModel);
    }

    @Test
    void shouldNotConvertStringToUri() {
        assertThrows(IllegalArgumentException.class, () -> {RdfUtils.toURI("example");});
    }

    @ParameterizedTest
    @ValueSource(strings = {"urn:example:example","http:/example/example.com"})
    void shouldConvertStringToUri(String string) {
        assertDoesNotThrow(() -> RdfUtils.toURI(string));
    }

    @Test
    void shouldSetLiteralLanguage() {
        String string = "example";
        Literal answer = RdfUtils.setLiteralLanguage(string);
        assertEquals("\"example\"^^<http://www.w3.org/2001/XMLSchema#language>",answer.toString());
    }

    @Test
    void shouldSetLiteralXML() {
        String string = "example";
        Literal answer = RdfUtils.setLiteralXML(string);
        assertEquals("\"example\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>",answer.toString());
    }

    @Test
    void shouldSetLiteralYear() {
        String string = "2025-04-09";
        Literal answer = RdfUtils.setLiteralYear(string);
        assertEquals("\"2025-04-09\"^^<http://www.w3.org/2001/XMLSchema#gYear>",answer.toString());
    }

    @Test
    void shouldSetLiteralDate() {
        String string = "2025-04-09";
        Literal answer = RdfUtils.setLiteralDate(string);
        assertEquals("\"2025-04-09\"^^<http://www.w3.org/2001/XMLSchema#date>",answer.toString());
    }

    @Test
    void shouldSetLiteralDateTime() {
        String string = "2025-04-09";
        Literal answer = RdfUtils.setLiteralDateTime(string);
        assertEquals("\"2025-04-09T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>",answer.toString());
    }

    @Test
    void shouldSetLiteralInt() {
        String string = "421";
        Literal answer = RdfUtils.setLiteralInt(string);
        assertEquals("\"421\"^^<http://www.w3.org/2001/XMLSchema#int>",answer.toString());
    }

    @Test
    void shouldCreateLiteral() {
        String string = "example";
        Literal answer = RdfUtils.createLiteral(string,objectIri);
        assertEquals("\"example\"^^<https://namespaceObjectlocalNameObject>",answer.toString());
    }

    @Test
    void shouldSetLiteralBoolean() {
        Boolean booleanTrue = true;
        Literal answer = RdfUtils.setLiteralBoolean(booleanTrue);
        assertEquals("\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>",answer.toString());
    }

    @Test
    void shouldSetLiteralString() {
        String stringWithSpace = "example        ";
        String stringWithoutSpace  = "example";
        String language= "language";
        ValidationStatus validationStatus = ValidationStatus.VALIDATED;

        Literal answerStringWithSpace = RdfUtils.setLiteralString(stringWithSpace,language);
        Literal answerStringWithoutSpace = RdfUtils.setLiteralString(stringWithoutSpace);
        Literal answerValidationStatus = RdfUtils.setLiteralString(validationStatus);

        boolean isAnswerStringWithSpaceCorrect ="\"example\"@language".equals(answerStringWithSpace.toString());
        boolean isAnswerStringWithoutSpaceCorrect ="\"example\"".equals(answerStringWithoutSpace.toString());
        boolean isAnswerValidationStatusCorrect ="\"Validated\"".equals(answerValidationStatus.toString());

        assertTrue(isAnswerStringWithSpaceCorrect && isAnswerStringWithoutSpaceCorrect && isAnswerValidationStatusCorrect );
    }

    @Test
    void testCreateBlankNode_ShouldReturnNonNullBNode() {
        BNode result = RdfUtils.createBlankNode();
        assertTrue(result.isBNode(), "The created blank node should be a blank node");
    }

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
        assertEquals("[(http://iri, http://predicate, http://iri/prefix/fr) [null], (http://iri/prefix/fr, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://rdf-vocabulary.ddialliance.org/xkos#ExplanatoryNote) [null], (http://iri/prefix/fr, http://eurovoc.europa.eu/schema#noteLiteral, \"<p>value</p>\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>) [null], (http://iri/prefix/fr, http://www.w3.org/2001/XMLSchema#language, \"fr\"^^<http://www.w3.org/2001/XMLSchema#language>) [null]]", model.toString());
    }
}