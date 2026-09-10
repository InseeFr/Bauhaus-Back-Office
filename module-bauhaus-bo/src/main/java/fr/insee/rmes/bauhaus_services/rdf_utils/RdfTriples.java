package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.utils.DateUtils;
import java.time.format.DateTimeFormatter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;

/**
 * Successeur sans état de {@link RdfUtils} pour la construction de triplets.
 *
 * <p>{@code RdfUtils} est déprécié pour suppression parce qu'il porte de l'état statique
 * (graphes et URI de base injectés après coup par {@code RdfUtilsInitializer}) : impossible à
 * configurer par test, invisible pour le conteneur Spring. Cette classe-ci ne reprend que les
 * fonctions réellement pures — littéraux, IRI, ajout de triplets dans un {@link Model} — ; la
 * partie qui dépend de la configuration vit dans {@link BauhausIriFactory}.
 *
 * <p>Les méthodes {@code add*} ignorent silencieusement une valeur absente : c'est ce qui permet
 * d'aligner les propriétés facultatives d'un objet sans tester chaque champ à l'appel.
 */
public final class RdfTriples {

    private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();

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

    private static boolean isFilled(String value) {
        return value != null && !value.isEmpty();
    }
}
