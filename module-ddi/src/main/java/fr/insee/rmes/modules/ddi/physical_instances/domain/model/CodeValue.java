package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code CodeValueType} du schéma DDI 4 : valeur tirée d'un vocabulaire contrôlé, accompagnée de
 * l'URI de ce vocabulaire (attribut {@code controlledVocabularyID} en DDI 3.3).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CodeValue(
        @JsonProperty("StringValue") String stringValue,
        @JsonProperty("ControlledVocabularyURI") String controlledVocabularyUri) {

    public static final String NUMERIC_TYPE_VOCABULARY_URI =
            "http://rdf-vocabulary.ddialliance.org/cv/NumericType/1.1.0/";
    public static final String NUMERIC_TYPE_VOCABULARY_VERSION = "1.1.0";
    public static final String DATE_TYPE_VOCABULARY_URI = "http://rdf-vocabulary.ddialliance.org/cv/DateType/1.1.2/";
    public static final String DATE_TYPE_VOCABULARY_VERSION = "1.1.2";

    public static CodeValue numericType(String value) {
        return new CodeValue(value, NUMERIC_TYPE_VOCABULARY_URI);
    }

    public static CodeValue dateType(String value) {
        return new CodeValue(value, DATE_TYPE_VOCABULARY_URI);
    }
}
