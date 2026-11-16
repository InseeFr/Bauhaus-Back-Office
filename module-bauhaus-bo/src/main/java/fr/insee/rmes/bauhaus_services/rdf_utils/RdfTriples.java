package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;

/**
 * Construction de triplets sans état : littéraux, IRI, ajout de triplets dans un {@link Model}.
 * Tout ce qui dépend de la configuration (graphes, URI de base) vit dans
 * {@link BauhausIriFactory}, injecté par Spring.
 *
 * <p>Les méthodes {@code add*} ignorent silencieusement une valeur absente : c'est ce qui permet
 * d'aligner les propriétés facultatives d'un objet sans tester chaque champ à l'appel.
 */
public final class RdfTriples {

    private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private RdfTriples() {}

    public static IRI iri(String value) {
        return FACTORY.createIRI(value.trim());
    }

    public static IRI xsdIri(String suffix) {
        return FACTORY.createIRI(XSD.NAMESPACE, suffix);
    }

    public static Literal string(String value) {
        return FACTORY.createLiteral(value.trim());
    }

    public static Literal string(String value, String language) {
        return FACTORY.createLiteral(value.trim(), language);
    }

    public static Literal string(ValidationStatus status) {
        return FACTORY.createLiteral(status.getValue().trim());
    }

    public static Literal dateTime(String date) {
        return FACTORY.createLiteral(
                DateTimeFormatter.ISO_DATE_TIME.format(DateUtils.parseDateTime(date)), XSD.DATETIME);
    }

    public static void addString(IRI subject, IRI predicate, String value, Model model, Resource graph) {
        if (isFilled(value)) {
            model.add(subject, predicate, string(value), graph);
        }
    }

    public static void addString(
            IRI subject, IRI predicate, String value, String language, Model model, Resource graph) {
        if (isFilled(value)) {
            model.add(subject, predicate, string(value, language), graph);
        }
    }

    public static void addUri(IRI subject, IRI predicate, IRI value, Model model, Resource graph) {
        if (value != null) {
            model.add(subject, predicate, value, graph);
        }
    }

    public static void addUri(Resource subject, IRI predicate, String value, Model model, Resource graph) {
        if (isFilled(value)) {
            model.add(subject, predicate, iri(value), graph);
        }
    }

    public static Literal bool(Boolean value) {
        return FACTORY.createLiteral(value);
    }

    public static Literal integer(String number) {
        return FACTORY.createLiteral(number, XSD.INT);
    }

    public static Literal date(String date) {
        return FACTORY.createLiteral(new SimpleDateFormat(DATE_FORMAT).format(DateUtils.parseDate(date)), XSD.DATE);
    }

    public static Literal xmlLiteral(String value) {
        return FACTORY.createLiteral(value.trim(), RDF.XMLLITERAL);
    }

    public static Literal language(String value) {
        return FACTORY.createLiteral(value.trim(), XSD.LANGUAGE);
    }

    public static void addDateTime(IRI subject, IRI predicate, String value, Model model, Resource graph) {
        if (isFilled(value)) {
            model.add(subject, predicate, dateTime(value), graph);
        }
    }

    public static void addDate(IRI subject, IRI predicate, String value, Model model, Resource graph) {
        if (isFilled(value)) {
            model.add(subject, predicate, date(value), graph);
        }
    }

    /**
     * IRI construite sans retouche de la valeur, à la différence de {@link #iri(String)} qui la
     * {@code trim()}, pour les appelants dont la valeur doit rester intacte.
     */
    public static IRI iriAsIs(String value) {
        return FACTORY.createIRI(value);
    }

    public static Literal literal(String value, IRI datatype) {
        return FACTORY.createLiteral(value, datatype);
    }

    public static BNode blankNode() {
        return FACTORY.createBNode();
    }

    public static void addInt(IRI subject, IRI predicate, String value, Model model, Resource graph) {
        if (isFilled(value)) {
            model.add(subject, predicate, integer(value), graph);
        }
    }

    public static void addStringMdToXhtml(
            IRI subject, IRI predicate, String value, String language, Model model, Resource graph) {
        if (isFilled(value)) {
            addString(subject, predicate, XhtmlToMarkdownUtils.markdownToXhtml(value), language, model, graph);
        }
    }

    public static Literal year(String date) {
        return FACTORY.createLiteral(new SimpleDateFormat(DATE_FORMAT).format(DateUtils.parseDate(date)), XSD.GYEAR);
    }

    public static void addBNode(Resource subject, IRI predicate, BNode value, Model model, Resource graph) {
        if (value != null) {
            model.add(subject, predicate, value, graph);
        }
    }

    private static boolean isFilled(String value) {
        return value != null && !value.isEmpty();
    }
}
