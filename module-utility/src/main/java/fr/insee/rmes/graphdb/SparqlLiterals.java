package fr.insee.rmes.graphdb;

import java.util.regex.Pattern;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.helpers.NTriplesUtil;

/**
 * Builds the SPARQL tokens used to inject a user-supplied value in a query.
 * <p>
 * Every method returns a <em>complete</em> token, delimiters included ({@code "…"}, {@code "…"@fr},
 * {@code <…>}) : the caller must never add its own quotes or angle brackets around the result,
 * otherwise the escaping is void. In a FreeMarker template, write {@code ${NOTATION}} and not
 * {@code '${NOTATION}'}.
 */
public final class SparqlLiterals {

    /**
     * SPARQL 1.1 grammar, rule [145] LANGTAG : {@code '@' [a-zA-Z]+ ('-' [a-zA-Z0-9]+)*}.
     */
    private static final Pattern LANGUAGE_TAG = Pattern.compile("[a-zA-Z]+(-[a-zA-Z0-9]+)*");

    /**
     * Characters an IRIREF may never contain, even escaped : they would close the {@code <…>} token
     * or be rejected by the parser. SPARQL 1.1 grammar, rule [139] IRIREF :
     * {@code '<' ([^<>"{}|^`\\] - [#x00-#x20])* '>'}.
     */
    private static final Pattern FORBIDDEN_IN_IRI_REF = Pattern.compile("[\\x00-\\x20<>\"{}|^`\\\\]");

    /**
     * SPARQL 1.1 grammar, rule [166] VARNAME, narrowed to ASCII : every variable our queries project
     * is ASCII camelCase, and a character set tighter than the grammar can only close doors.
     */
    private static final Pattern VARIABLE_NAME = Pattern.compile("\\w+");

    private SparqlLiterals() {
        // utility class
    }

    /**
     * @param value the raw value, may contain any character
     * @return the value as a quoted SPARQL string literal, e.g. {@code "Libellé"}
     */
    public static String literal(String value) {
        rejectNull(value, "value");
        return "\"" + NTriplesUtil.escapeString(value) + "\"";
    }

    /**
     * @param value       the raw value, may contain any character
     * @param languageTag a valid SPARQL langtag, e.g. {@code fr} or {@code en-GB}
     * @return the value as a language-tagged SPARQL string literal, e.g. {@code "Libellé"@fr}
     */
    public static String literal(String value, String languageTag) {
        rejectNull(languageTag, "languageTag");
        if (!LANGUAGE_TAG.matcher(languageTag).matches()) {
            throw new IllegalArgumentException("Not a valid SPARQL language tag: " + languageTag);
        }
        return literal(value) + "@" + languageTag;
    }

    /**
     * @param value an absolute IRI
     * @return the IRI as a SPARQL IRIREF, e.g. {@code <http://rdf.insee.fr/def/base#Concept>}
     * @throws IllegalArgumentException if the value is not an absolute IRI, or contains a character
     *                                  forbidden in an IRIREF
     */
    public static String iri(String value) {
        rejectNull(value, "iri");
        if (FORBIDDEN_IN_IRI_REF.matcher(value).find()) {
            throw new IllegalArgumentException("Character forbidden in an IRI: " + value);
        }
        IRI parsed = SimpleValueFactory.getInstance().createIRI(value);
        return NTriplesUtil.toNTriplesString(parsed);
    }

    /**
     * A variable cannot be escaped : its name is part of the query structure, not of its data. It is
     * therefore validated, not encoded — anything that is not a VARNAME is rejected.
     *
     * @param name the variable name, without its leading {@code ?}
     * @return the variable as a complete SPARQL token, e.g. {@code ?labelLg1}
     * @throws IllegalArgumentException if the name is not a valid SPARQL variable name
     */
    public static String variable(String name) {
        rejectNull(name, "variable name");
        if (!VARIABLE_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Not a valid SPARQL variable name: " + name);
        }
        return "?" + name;
    }

    private static void rejectNull(String value, String name) {
        if (value == null) {
            throw new IllegalArgumentException("The " + name + " to inject in a query must not be null");
        }
    }
}
