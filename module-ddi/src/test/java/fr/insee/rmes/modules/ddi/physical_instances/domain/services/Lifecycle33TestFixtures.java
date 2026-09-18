package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import java.util.HashMap;
import java.util.List;
import org.apache.xmlbeans.XmlOptions;

/** Fixtures shared by the DDI 3.3 (lifecycle) &lt;-&gt; DDI 4 converter tests. */
final class Lifecycle33TestFixtures {

    static final String DDI_INSTANCE_NS = "ddi:instance:3_3";
    static final String DDI_REUSABLE_NS = "ddi:reusable:3_3";
    static final String LOGICAL_PRODUCT_NS = "ddi:logicalproduct:3_3";
    static final String PHYSICAL_INSTANCE_NS = "ddi:physicalinstance:3_3";
    static final String GROUP_NS = "ddi:group:3_3";
    static final String STUDY_UNIT_NS = "ddi:studyunit:3_3";

    private Lifecycle33TestFixtures() {}

    /** Wraps the content in a DDI 3.3 Fragment declaring the instance (default) and reusable (r) namespaces. */
    static String fragment(String content) {
        return "<Fragment xmlns=\"" + DDI_INSTANCE_NS + "\" xmlns:r=\"" + DDI_REUSABLE_NS + "\">" + content
                + "</Fragment>";
    }

    /** Attributes of a universally unique item declared in the given default namespace. */
    static String inNamespace(String namespace, String versionDate) {
        return "xmlns=\"" + namespace + "\" " + universallyUnique(versionDate);
    }

    /** Attributes of a universally unique item. */
    static String universallyUnique(String versionDate) {
        return "isUniversallyUnique=\"true\" versionDate=\"" + versionDate + "\"";
    }

    /** A versionable item of agency fr.insee in version 1: URN, Agency, ID and Version, then the body. */
    static String versionable(String element, String attributes, String urn, String id, String body) {
        return "<" + element + " " + attributes + ">"
                + "<r:URN>" + urn + "</r:URN>"
                + "<r:Agency>fr.insee</r:Agency><r:ID>" + id + "</r:ID><r:Version>1</r:Version>"
                + body
                + "</" + element + ">";
    }

    /** A reference element to the fr.insee item {@code id} in version 1. */
    static String reference(String element, String id, String typeOfObject) {
        return "<" + element + ">"
                + "<r:Agency>fr.insee</r:Agency><r:ID>" + id + "</r:ID><r:Version>1</r:Version>"
                + "<r:TypeOfObject>" + typeOfObject + "</r:TypeOfObject>"
                + "</" + element + ">";
    }

    /** Fragment of the sentinel values MMVR (#1566) referencing the cl-sentinelles code list. */
    static String sentinelValuesMmvrFragment() {
        return fragment(versionable(
                "r:ManagedMissingValuesRepresentation",
                universallyUnique("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:mmvr-1:1",
                "mmvr-1",
                """
                <r:Label><r:Content xml:lang="fr-FR">Valeurs sentinelles NSP/REF</r:Content></r:Label>
                <r:MissingCodeRepresentation blankIsMissingValue="false">
                    <r:CodeListReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>cl-sentinelles</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>CodeList</r:TypeOfObject>
                    </r:CodeListReference>
                </r:MissingCodeRepresentation>
                """));
    }

    /** The sentinel values MMVR (#1566) referencing the cl-sentinelles code list. */
    static Ddi4ManagedMissingValuesRepresentation sentinelValuesMmvr(CogsDate versionDate) {
        return new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                versionDate,
                "urn:ddi:fr.insee:mmvr-1:1",
                "fr.insee",
                "mmvr-1",
                "1",
                LangStrings.of("fr-FR", "Valeurs sentinelles NSP/REF"),
                List.of(new CodeRepresentation(
                        CodeRepresentation.TYPE, false, Reference.of("fr.insee", "cl-sentinelles", "1", "CodeList"))));
    }

    /** The lp-id LogicalProduct, with the given scheme references. */
    static Ddi4LogicalProduct logicalProduct(
            String label,
            List<Reference> codeListSchemeReference,
            List<Reference> categorySchemeReference,
            List<Reference> variableSchemeReference,
            List<Reference> managedRepresentationSchemeReference) {
        return new Ddi4LogicalProduct(
                Ddi4LogicalProduct.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:lp-id:1",
                "fr.insee",
                "lp-id",
                "1",
                LangStrings.of("fr-FR", label),
                codeListSchemeReference,
                categorySchemeReference,
                variableSchemeReference,
                managedRepresentationSchemeReference);
    }

    /** The lp-id LogicalProduct referencing one scheme of each kind (cls-1, cats-1, vars-1, mrs-1). */
    static Ddi4LogicalProduct logicalProductReferencingEverySchemeKind(String label) {
        return logicalProduct(
                label,
                List.of(Reference.of("fr.insee", "cls-1", "1", "CodeListScheme")),
                List.of(Reference.of("fr.insee", "cats-1", "1", "CategoryScheme")),
                List.of(Reference.of("fr.insee", "vars-1", "1", "VariableScheme")),
                List.of(Reference.of("fr.insee", "mrs-1", "1", "ManagedRepresentationScheme")));
    }

    /** Serialization options producing a Fragment whose content element is in the given namespace. */
    static XmlOptions fragmentOptions(String contentNs) {
        HashMap<String, String> prefixes = new HashMap<>();
        prefixes.put(DDI_INSTANCE_NS, "");
        prefixes.put(contentNs, "");
        prefixes.put(DDI_REUSABLE_NS, "r");
        XmlOptions options = new XmlOptions();
        options.setSaveSuggestedPrefixes(prefixes);
        return options;
    }
}
