package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.ddi.lifecycle33.instance.FragmentType;
import fr.insee.ddi.lifecycle33.logicalproduct.CategoryType;
import fr.insee.ddi.lifecycle33.logicalproduct.CodeListType;
import fr.insee.ddi.lifecycle33.logicalproduct.CodeType;
import fr.insee.ddi.lifecycle33.logicalproduct.DataRelationshipType;
import fr.insee.ddi.lifecycle33.logicalproduct.LogicalRecordType;
import fr.insee.ddi.lifecycle33.logicalproduct.VariableRepresentationType;
import fr.insee.ddi.lifecycle33.logicalproduct.VariableType;
import fr.insee.ddi.lifecycle33.logicalproduct.VariablesInRecordType;
import fr.insee.ddi.lifecycle33.physicalinstance.PhysicalInstanceType;
import fr.insee.ddi.lifecycle33.reusable.AbstractIdentifiableType;
import fr.insee.ddi.lifecycle33.reusable.AbstractVersionableType;
import fr.insee.ddi.lifecycle33.reusable.BasedOnObjectType;
import fr.insee.ddi.lifecycle33.reusable.CitationType;
import fr.insee.ddi.lifecycle33.reusable.ContentType;
import fr.insee.ddi.lifecycle33.reusable.InternationalStringType;
import fr.insee.ddi.lifecycle33.reusable.LabelType;
import fr.insee.ddi.lifecycle33.reusable.NameType;
import fr.insee.ddi.lifecycle33.reusable.ReferenceType;
import fr.insee.ddi.lifecycle33.reusable.StringType;
import fr.insee.ddi.lifecycle33.reusable.StructuredStringType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Category;
import fr.insee.rmes.modules.ddi.physical_instances.generated.CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.generated.DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.generated.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.generated.PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Variable;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Lecteur de fragments DDI 3.3 s'appuyant sur les classes generees par ddi-lifecycle (XmlBeans).
 * <p>
 * Le fragment est parse via {@link FragmentDocument}, puis chaque type DDI est navigue par ses
 * accesseurs types ({@code getURNArray}, {@code getCitation}, {@code getLabelArray}...). Seuls les
 * corps de {@code VariableRepresentation} (CodeRepresentation, NumericRepresentation, etc.) restent
 * lus via un {@link XmlCursor} : ce sont des membres de groupes de substitution exposes derriere le
 * type abstrait {@code RepresentationType}, exactement comme {@code Ddi3XmlWriter} les ecrit au
 * curseur. Le contrat de sortie est inchange : memes setters et memes entrees
 * {@code additionalProperties} sous leurs cles JSON historiques.
 */
public class Ddi3XmlReader {

    public static final String IS_UNIVERSALLY_UNIQUE = "isUniversallyUnique";
    public static final String VERSION_DATE = "versionDate";
    public static final String URN = "URN";
    public static final String AGENCY = "Agency";
    public static final String ID = "ID";
    public static final String VERSION = "Version";
    private static final String TYPE_OF_OBJECT = "TypeOfObject";

    public Ddi3XmlReader() {
        // Aucune dependance : la lecture se fait via les fabriques XmlBeans de ddi-lifecycle.
    }

    public PhysicalInstance parsePhysicalInstance(String xmlFragment) throws Exception {
        FragmentType fragment = FragmentDocument.Factory.parse(xmlFragment).getFragment();
        if (!fragment.isSetPhysicalInstance()) {
            throw new IllegalArgumentException("No PhysicalInstance element found");
        }
        PhysicalInstanceType piElement = fragment.getPhysicalInstance();

        PhysicalInstance physicalInstance = new PhysicalInstance();
        physicalInstance.setURN(urn(piElement));
        physicalInstance.setAgency(agency(piElement));
        physicalInstance.setID(id(piElement));
        physicalInstance.setVersion(version(piElement));
        putDdi3Attributes(physicalInstance::putAdditionalProperty, piElement);
        BasedOnObject basedOnObject = parseBasedOnObject(piElement);
        if (basedOnObject != null) {
            physicalInstance.putAdditionalProperty("BasedOnObject", basedOnObject);
        }
        Citation citation = parseCitation(piElement);
        if (citation != null) {
            physicalInstance.putAdditionalProperty("Citation", citation);
        }
        DataRelationshipReference dataRelationshipReference = parseDataRelationshipReference(piElement);
        if (dataRelationshipReference != null) {
            physicalInstance.putAdditionalProperty("DataRelationshipReference", dataRelationshipReference);
        }
        return physicalInstance;
    }

    public DataRelationship parseDataRelationship(String xmlFragment) throws Exception {
        FragmentType fragment = FragmentDocument.Factory.parse(xmlFragment).getFragment();
        if (!fragment.isSetDataRelationship()) {
            throw new IllegalArgumentException("No DataRelationship element found");
        }
        DataRelationshipType drElement = fragment.getDataRelationship();

        DataRelationship dataRelationship = new DataRelationship();
        dataRelationship.setURN(urn(drElement));
        dataRelationship.setAgency(agency(drElement));
        dataRelationship.setID(id(drElement));
        dataRelationship.setVersion(version(drElement));
        dataRelationship.setLabel(labelOrName(drElement.getLabelArray(), drElement.getDataRelationshipNameArray()));
        putDdi3Attributes(dataRelationship::putAdditionalProperty, drElement);
        BasedOnObject basedOnObject = parseBasedOnObject(drElement);
        if (basedOnObject != null) {
            dataRelationship.putAdditionalProperty("BasedOnObject", basedOnObject);
        }
        LogicalRecord logicalRecord = parseLogicalRecord(drElement);
        if (logicalRecord != null) {
            dataRelationship.putAdditionalProperty("LogicalRecord", logicalRecord);
        }
        return dataRelationship;
    }

    public Variable parseVariable(String xmlFragment) throws Exception {
        FragmentType fragment = FragmentDocument.Factory.parse(xmlFragment).getFragment();
        if (!fragment.isSetVariable()) {
            throw new IllegalArgumentException("No Variable element found");
        }
        VariableType varElement = fragment.getVariable();

        Variable variable = new Variable();
        variable.setURN(urn(varElement));
        variable.setAgency(agency(varElement));
        variable.setID(id(varElement));
        variable.setVersion(version(varElement));
        variable.setVariableName(firstName(varElement.getVariableNameArray()));
        variable.setLabel(firstLabel(varElement.getLabelArray()));
        variable.setDescription(firstContent(varElement.getDescription()));
        putDdi3Attributes(variable::putAdditionalProperty, varElement);
        variable.putAdditionalProperty("@isGeographic", geographic(varElement));
        // BasedOnObject et VariableRepresentation : formes historiques conservees, portees par
        // additionalProperties sous leurs cles JSON pour preserver le contrat et le round-trip.
        BasedOnObject basedOnObject = parseBasedOnObject(varElement);
        if (basedOnObject != null) {
            variable.putAdditionalProperty("BasedOnObject", basedOnObject);
        }
        VariableRepresentation representation = parseVariableRepresentation(varElement);
        if (representation != null) {
            variable.putAdditionalProperty("VariableRepresentation", representation);
        }
        return variable;
    }

    public CodeList parseCodeList(String xmlFragment) throws Exception {
        FragmentType fragment = FragmentDocument.Factory.parse(xmlFragment).getFragment();
        if (!fragment.isSetCodeList()) {
            throw new IllegalArgumentException("No CodeList element found");
        }
        CodeListType clElement = fragment.getCodeList();

        CodeList codeList = new CodeList();
        codeList.setURN(urn(clElement));
        codeList.setAgency(agency(clElement));
        codeList.setID(id(clElement));
        codeList.setVersion(version(clElement));
        codeList.setLabel(firstLabel(clElement.getLabelArray()));
        putDdi3Attributes(codeList::putAdditionalProperty, clElement);
        // Les Code (avec CategoryReference en forme historique) restent des records, portes par
        // additionalProperties sous la cle JSON "Code" : contrat et round-trip preserves.
        codeList.putAdditionalProperty("Code", parseCodes(clElement));
        return codeList;
    }

    public Category parseCategory(String xmlFragment) throws Exception {
        FragmentType fragment = FragmentDocument.Factory.parse(xmlFragment).getFragment();
        if (!fragment.isSetCategory()) {
            throw new IllegalArgumentException("No Category element found");
        }
        CategoryType catElement = fragment.getCategory();

        Category category = new Category();
        category.setURN(urn(catElement));
        category.setAgency(agency(catElement));
        category.setID(id(catElement));
        category.setVersion(version(catElement));
        category.setLabel(firstLabel(catElement.getLabelArray()));
        putDdi3Attributes(category::putAdditionalProperty, catElement);
        return category;
    }

    /**
     * Porte les attributs DDI3 absents du schema DDI4 ({@code @isUniversallyUnique},
     * {@code @versionDate}) dans la Map {@code additionalProperties} du POJO genere, sous leurs
     * cles JSON historiques. Le contrat JSON expose au front et le round-trip XML restent identiques.
     */
    private void putDdi3Attributes(BiConsumer<String, Object> sink, AbstractVersionableType element) {
        sink.accept("@isUniversallyUnique", isUniversallyUnique(element));
        sink.accept("@versionDate", versionDate(element));
    }

    // ----------------------------------------------------------------------------------------
    // Accesseurs typés : champs versionnables, libellés, citations, références.
    // ----------------------------------------------------------------------------------------

    private Citation parseCitation(PhysicalInstanceType pi) {
        CitationType citation = pi.getCitation();
        if (citation == null) return null;

        InternationalStringType title = citation.getTitle();
        if (title == null || title.getStringArray().length == 0) return null;

        return new Citation(fromString(title.getStringArray(0)));
    }

    private DataRelationshipReference parseDataRelationshipReference(PhysicalInstanceType pi) {
        // Les payloads Colectica emettent <DataRelationshipReference> dans le namespace
        // physicalinstance (et non reusable) : l'accesseur type ne le lie pas. On le lit donc par
        // nom local, comme les references du sous-arbre VariableRepresentation.
        XmlObject ref = childElement(pi, "DataRelationshipReference");
        if (ref == null) return null;
        return new DataRelationshipReference(
            text(ref, AGENCY), text(ref, ID), text(ref, VERSION), text(ref, TYPE_OF_OBJECT));
    }

    private BasedOnObject parseBasedOnObject(AbstractVersionableType versionable) {
        if (!versionable.isSetBasedOnObject()) return null;
        BasedOnObjectType basedOnObject = versionable.getBasedOnObject();
        if (basedOnObject.sizeOfBasedOnReferenceArray() == 0) return null;

        ReferenceType ref = basedOnObject.getBasedOnReferenceArray(0);
        return new BasedOnObject(new BasedOnReference(refAgency(ref), refId(ref), refVersion(ref), refType(ref)));
    }

    private LogicalRecord parseLogicalRecord(DataRelationshipType dr) {
        if (dr.sizeOfLogicalRecordArray() == 0) return null;
        LogicalRecordType lr = dr.getLogicalRecordArray(0);

        return new LogicalRecord(
            isUniversallyUnique(lr),
            urn(lr),
            agency(lr),
            id(lr),
            version(lr),
            labelOrName(lr.getLabelArray(), lr.getLogicalRecordNameArray()),
            parseVariablesInRecord(lr)
        );
    }

    private VariablesInRecord parseVariablesInRecord(LogicalRecordType lr) {
        VariablesInRecordType vir = lr.getVariablesInRecord();
        if (vir == null) return null;

        ReferenceType[] varRefs = vir.getVariableUsedReferenceArray();
        if (varRefs.length == 0) return null;

        List<VariableUsedReference> refs = new ArrayList<>();
        for (ReferenceType ref : varRefs) {
            refs.add(new VariableUsedReference(refAgency(ref), refId(ref), refVersion(ref), refType(ref)));
        }
        return new VariablesInRecord(refs);
    }

    private List<Code> parseCodes(CodeListType codeList) {
        CodeType[] codeNodes = codeList.getCodeArray();
        if (codeNodes.length == 0) return null;

        List<Code> codes = new ArrayList<>();
        for (CodeType code : codeNodes) {
            CategoryReference catRef = null;
            ReferenceType catRefElement = code.getCategoryReference();
            if (catRefElement != null) {
                catRef = new CategoryReference(
                    refAgency(catRefElement), refId(catRefElement), refVersion(catRefElement), refType(catRefElement));
            }
            String value = code.getValue() == null ? "" : nz(code.getValue().getStringValue());
            codes.add(new Code(
                isUniversallyUnique(code), urn(code), agency(code), id(code), version(code), catRef, value));
        }
        return codes;
    }

    /** Premier libellé {@code Label} ; à défaut, repli sur le {@code Name} (DataRelationshipName, etc.). */
    private static List<LangString> labelOrName(LabelType[] labels, NameType[] names) {
        List<LangString> label = firstLabel(labels);
        return label != null ? label : firstName(names);
    }

    private static List<LangString> firstLabel(LabelType[] labels) {
        if (labels.length == 0 || labels[0].getContentArray().length == 0) return null;
        return fromContent(labels[0].getContentArray(0));
    }

    private static List<LangString> firstName(NameType[] names) {
        if (names.length == 0 || names[0].getStringArray().length == 0) return null;
        return fromString(names[0].getStringArray(0));
    }

    private static List<LangString> firstContent(StructuredStringType structured) {
        if (structured == null || structured.getContentArray().length == 0) return null;
        return fromContent(structured.getContentArray(0));
    }

    private static List<LangString> fromString(StringType string) {
        return LangStrings.of(nz(string.getLang()), nz(string.getStringValue()));
    }

    private static List<LangString> fromContent(ContentType content) {
        // ContentType est un contenu mixte (XHTML) sans getStringValue() : on concatene son texte
        // au curseur, equivalent de l'ancien Node#getTextContent().
        return LangStrings.of(nz(content.getLang()), textContent(content));
    }

    // Champs versionnables / identifiables : valeur lexicale, "" si absente (comme l'ancien DOM).

    private static String urn(AbstractIdentifiableType v) {
        return v.sizeOfURNArray() == 0 ? "" : nz(v.getURNArray(0).getStringValue());
    }

    private static String agency(AbstractIdentifiableType v) {
        return v.sizeOfAgencyArray() == 0 ? "" : nz(v.getAgencyArray(0));
    }

    private static String id(AbstractIdentifiableType v) {
        return v.sizeOfIDArray() == 0 ? "" : nz(v.getIDArray(0).getStringValue());
    }

    private static String version(AbstractIdentifiableType v) {
        return v.sizeOfVersionArray() == 0 ? "" : nz(v.getVersionArray(0));
    }

    private static String isUniversallyUnique(AbstractIdentifiableType v) {
        return v.isSetIsUniversallyUnique() ? v.xgetIsUniversallyUnique().getStringValue() : "";
    }

    private static String versionDate(AbstractVersionableType v) {
        return v.isSetVersionDate() ? v.xgetVersionDate().getStringValue() : "";
    }

    private static String geographic(VariableType v) {
        return v.isSetIsGeographic() ? v.xgetIsGeographic().getStringValue() : "";
    }

    private static String refAgency(ReferenceType r) {
        return r.getAgencyArray().length == 0 ? "" : nz(r.getAgencyArray(0));
    }

    private static String refId(ReferenceType r) {
        return r.getIDArray().length == 0 ? "" : nz(r.getIDArray(0).getStringValue());
    }

    private static String refVersion(ReferenceType r) {
        return r.getVersionArray().length == 0 ? "" : nz(r.getVersionArray(0));
    }

    private static String refType(ReferenceType r) {
        return r.getTypeOfObject() == null ? "" : r.getTypeOfObject().toString();
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }

    // ----------------------------------------------------------------------------------------
    // VariableRepresentation : les corps (CodeRepresentation, NumericRepresentation, ...) sont des
    // membres de groupes de substitution exposes derriere le type abstrait RepresentationType.
    // Comme Ddi3XmlWriter les ecrit au curseur, on les relit par parcours XmlCursor du sous-arbre
    // (nom local, ordre document) — borne au sous-arbre de VariableRepresentation.
    // ----------------------------------------------------------------------------------------

    private VariableRepresentation parseVariableRepresentation(VariableType var) {
        VariableRepresentationType vr = var.getVariableRepresentation();
        if (vr == null) return new VariableRepresentation(null, null, null, null, null);

        String role = vr.getVariableRole() == null ? "" : nz(vr.getVariableRole().getStringValue());
        CodeRepresentation codeRep = parseCodeRepresentation(vr);
        NumericRepresentation numRep = parseNumericRepresentation(vr);
        DateTimeRepresentation dateTimeRep = parseDateTimeRepresentation(vr);
        TextRepresentation textRep = parseTextRepresentation(vr);

        return new VariableRepresentation(
            role.isEmpty() ? null : role,
            codeRep,
            numRep,
            dateTimeRep,
            textRep
        );
    }

    private NumericRepresentation parseNumericRepresentation(XmlObject parent) {
        XmlObject numRepElement = childElement(parent, "NumericRepresentation");
        if (numRepElement == null) return null;

        String typeCode = text(numRepElement, "NumericTypeCode");
        NumberRange numberRange = parseNumberRange(numRepElement);

        return new NumericRepresentation(typeCode.isEmpty() ? null : typeCode, numberRange);
    }

    private NumberRange parseNumberRange(XmlObject parent) {
        XmlObject rangeElement = childElement(parent, "NumberRange");
        if (rangeElement == null) return null;

        RangeValue low = null;
        RangeValue high = null;

        XmlObject lowElement = childElement(rangeElement, "Low");
        if (lowElement != null) {
            low = new RangeValue(attribute(lowElement, "isInclusive"), textContent(lowElement));
        }
        XmlObject highElement = childElement(rangeElement, "High");
        if (highElement != null) {
            high = new RangeValue(attribute(highElement, "isInclusive"), textContent(highElement));
        }
        return new NumberRange(low, high);
    }

    private CodeRepresentation parseCodeRepresentation(XmlObject parent) {
        XmlObject codeRepElement = childElement(parent, "CodeRepresentation");
        if (codeRepElement == null) return null;

        String blankIsMissing = attribute(codeRepElement, "blankIsMissingValue");
        CodeListReference codeListRef = null;

        XmlObject refElement = childElement(codeRepElement, "CodeListReference");
        if (refElement != null) {
            codeListRef = new CodeListReference(
                text(refElement, AGENCY),
                text(refElement, ID),
                text(refElement, VERSION),
                text(refElement, TYPE_OF_OBJECT)
            );
        }
        return new CodeRepresentation(blankIsMissing, codeListRef);
    }

    private TextRepresentation parseTextRepresentation(XmlObject parent) {
        XmlObject textRepElement = childElement(parent, "TextRepresentation");
        if (textRepElement == null) return null;

        String blankIsMissing = attribute(textRepElement, "blankIsMissingValue");
        String regExp = text(textRepElement, "RegExp");
        Integer minLength = parseIntOrNull(text(textRepElement, "MinLength"));
        Integer maxLength = parseIntOrNull(text(textRepElement, "MaxLength"));

        return new TextRepresentation(maxLength, minLength, regExp.isEmpty() ? null : regExp, blankIsMissing);
    }

    private DateTimeRepresentation parseDateTimeRepresentation(XmlObject parent) {
        XmlObject dateTimeRepElement = childElement(parent, "DateTimeRepresentation");
        if (dateTimeRepElement == null) return null;

        String dateTypeCode = text(dateTimeRepElement, "DateTypeCode");
        String dateFieldFormat = text(dateTimeRepElement, "DateFieldFormat");

        return new DateTimeRepresentation(
            dateTypeCode.isEmpty() ? null : dateTypeCode,
            dateFieldFormat.isEmpty() ? null : dateFieldFormat
        );
    }

    private static Integer parseIntOrNull(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException _) {
            return null; // valeur non numerique ignoree, comme l'ancien comportement
        }
    }

    // Helpers XmlCursor (uniquement pour le sous-arbre VariableRepresentation, cf. commentaire ci-dessus).

    /** Texte du premier descendant de nom local {@code tagName} ; {@code ""} si absent. */
    private String text(XmlObject parent, String tagName) {
        XmlObject child = childElement(parent, tagName);
        return child == null ? "" : textContent(child);
    }

    /** Premier descendant de nom local {@code tagName}, ou {@code null} si absent. */
    private XmlObject childElement(XmlObject parent, String tagName) {
        try (XmlCursor cursor = parent.newCursor()) {
            // Curseur positionne sur le START de l'element courant : on parcourt sa descendance
            // jusqu'a son END (profondeur < 0), sans deborder sur ses freres.
            int depth = 0;
            XmlCursor.TokenType token = cursor.toNextToken();
            while (!token.isEnddoc() && !token.isNone()) {
                if (token.isStart()) {
                    QName name = cursor.getName();
                    if (name != null && tagName.equals(name.getLocalPart())) {
                        return cursor.getObject();
                    }
                    depth++;
                } else if (token.isEnd() && --depth < 0) {
                    break;
                }
                token = cursor.toNextToken();
            }
        }
        return null;
    }

    /** Valeur textuelle complete de l'element (equivalent de {@code Node#getTextContent()}). */
    private static String textContent(XmlObject element) {
        try (XmlCursor cursor = element.newCursor()) {
            return cursor.getTextValue();
        }
    }

    /** Valeur d'attribut sans espace de noms ; {@code ""} si absent (comme l'ancien helper DOM). */
    private String attribute(XmlObject element, String attributeName) {
        try (XmlCursor cursor = element.newCursor()) {
            String value = cursor.getAttributeText(new QName(attributeName));
            return value == null ? "" : value;
        }
    }
}
