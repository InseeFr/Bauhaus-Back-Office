package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;

public interface DDI3toDDI4ConverterService {
    /**
     * Convert DDI3 format to DDI4 format
     * @param ddi3 DDI3 data
     * @param schemaUrl URL of the DDI4 JSON schema
     * @return DDI4 formatted data
     */
    Ddi4Response convertDdi3ToDdi4(Ddi3Response ddi3, String schemaUrl);

    /**
     * Parse a single CodeListScheme from its DDI 3.3 fragment XML (as returned by Colectica
     * {@code GET item}). Used to read the existing scheme of a group before merging new
     * code list references into it.
     * @param fragmentXml the DDI 3.3 {@code <Fragment>} XML containing a CodeListScheme
     * @return the parsed code list scheme
     */
    Ddi4CodeListScheme toCodeListScheme(String fragmentXml);

    /**
     * Parse a single Group from its DDI 3.3 fragment XML (as returned by Colectica
     * {@code GET item}). Used to re-register a group with an added LogicalProductReference when its
     * CodeListScheme is auto-provisioned on save.
     * @param fragmentXml the DDI 3.3 {@code <Fragment>} XML containing a Group
     * @return the parsed group
     */
    Ddi4Group toGroup(String fragmentXml);

    /**
     * Parse a single CategoryScheme from its DDI 3.3 fragment XML. Used to read a group's existing
     * category scheme before merging new category references into it.
     * @param fragmentXml the DDI 3.3 {@code <Fragment>} XML containing a CategoryScheme
     * @return the parsed category scheme
     */
    Ddi4CategoryScheme toCategoryScheme(String fragmentXml);

    /**
     * Parse a single VariableScheme from its DDI 3.3 fragment XML. Used to read a study unit's
     * existing variable scheme before merging new variable references into it.
     * @param fragmentXml the DDI 3.3 {@code <Fragment>} XML containing a VariableScheme
     * @return the parsed variable scheme
     */
    Ddi4VariableScheme toVariableScheme(String fragmentXml);

    /**
     * Parse a single LogicalProduct from its DDI 3.3 fragment XML. Used to read the LogicalProduct a
     * container already exposes, so an auto-provisioned scheme is filed under it rather than under a
     * second LogicalProduct.
     * @param fragmentXml the DDI 3.3 {@code <Fragment>} XML containing a LogicalProduct
     * @return the parsed logical product
     */
    Ddi4LogicalProduct toLogicalProduct(String fragmentXml);

    /**
     * Parse a single StudyUnit from its DDI 3.3 fragment XML. Used to re-register a study unit with
     * an added LogicalProductReference when its VariableScheme is auto-provisioned on save.
     * @param fragmentXml the DDI 3.3 {@code <Fragment>} XML containing a StudyUnit
     * @return the parsed study unit
     */
    Ddi4StudyUnit toStudyUnit(String fragmentXml);

    /**
     * Parse a single ManagedRepresentationScheme from its DDI 3.3 fragment XML. Used to read a
     * group's existing managed representation scheme before merging new
     * ManagedMissingValuesRepresentation references into it (#1566).
     * @param fragmentXml the DDI 3.3 {@code <Fragment>} XML containing a ManagedRepresentationScheme
     * @return the parsed managed representation scheme
     */
    Ddi4ManagedRepresentationScheme toManagedRepresentationScheme(String fragmentXml);

    /**
     * Parse a single ManagedMissingValuesRepresentation from its DDI 3.3 fragment XML. Used to list
     * the reusable sentinel-value representations of a group (#1566).
     * @param fragmentXml the DDI 3.3 {@code <Fragment>} XML containing a
     *                    ManagedMissingValuesRepresentation
     * @return the parsed managed missing values representation
     */
    Ddi4ManagedMissingValuesRepresentation toManagedMissingValuesRepresentation(String fragmentXml);

    /**
     * Parse a single CodeList from its DDI 3.3 fragment XML. Used to build the code preview of the
     * sentinel CodeList referenced by a ManagedMissingValuesRepresentation (#1566).
     * @param fragmentXml the DDI 3.3 {@code <Fragment>} XML containing a CodeList
     * @return the parsed code list
     */
    Ddi4CodeList toCodeList(String fragmentXml);
}