package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

/** Clés de la map {@code itemTypes} de {@code colectica.yml}, seule source des UUID de type Colectica. */
final class ColecticaItemTypes {

    /** Clé {@code itemTypes} du type PhysicalInstance, aussi utilisée comme nom d'élément XML. */
    static final String PHYSICAL_INSTANCE = "PhysicalInstance";
    /** Clé {@code itemTypes} du type CodeList, aussi utilisée comme nom d'élément XML. */
    static final String CODE_LIST = "CodeList";

    static final String CATEGORY = "Category";
    static final String VARIABLE = "Variable";
    static final String DATA_RELATIONSHIP = "DataRelationship";
    static final String LOGICAL_PRODUCT = "LogicalProduct";
    static final String STUDY_UNIT = "StudyUnit";
    static final String GROUP = "Group";
    /** Regroupe des variables (ou d'autres groupes) sous un VariableScheme (#1387). */
    static final String VARIABLE_GROUP = "VariableGroup";

    static final String CODE_LIST_SCHEME = "CodeListScheme";
    static final String CATEGORY_SCHEME = "CategoryScheme";
    static final String VARIABLE_SCHEME = "VariableScheme";
    static final String MANAGED_REPRESENTATION_SCHEME = "ManagedRepresentationScheme";
    static final String MANAGED_MISSING_VALUES_REPRESENTATION = "ManagedMissingValuesRepresentation";

    private ColecticaItemTypes() {}
}
