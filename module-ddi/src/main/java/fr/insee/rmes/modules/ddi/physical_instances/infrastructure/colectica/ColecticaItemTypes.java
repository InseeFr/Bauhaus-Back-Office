package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

/**
 * Clés de la map {@code itemTypes} de la configuration, et UUID des types que Colectica n'y déclare
 * pas. {@code Group} en particulier est absent de la configuration : y accéder par
 * {@code itemTypes().get("Group")} renvoie {@code null} en production.
 */
final class ColecticaItemTypes {

    static final String GROUP_UUID = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";
    static final String STUDY_UNIT_UUID = "30ea0200-7121-4f01-8d21-a931a182b86d";

    /** Clé {@code itemTypes} du type PhysicalInstance, aussi utilisée comme nom d'élément XML. */
    static final String PHYSICAL_INSTANCE = "PhysicalInstance";
    /** Clé {@code itemTypes} du type CodeList, aussi utilisée comme nom d'élément XML. */
    static final String CODE_LIST = "CodeList";

    static final String CATEGORY = "Category";
    static final String VARIABLE = "Variable";
    static final String DATA_RELATIONSHIP = "DataRelationship";
    static final String LOGICAL_PRODUCT = "LogicalProduct";
    static final String STUDY_UNIT = "StudyUnit";
    static final String CODE_LIST_SCHEME = "CodeListScheme";
    static final String CATEGORY_SCHEME = "CategoryScheme";
    static final String VARIABLE_SCHEME = "VariableScheme";
    static final String MANAGED_REPRESENTATION_SCHEME = "ManagedRepresentationScheme";
    static final String MANAGED_MISSING_VALUES_REPRESENTATION = "ManagedMissingValuesRepresentation";

    private ColecticaItemTypes() {}
}
