package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.ddi.lifecycle33.instance.FragmentInstanceDocument;
import fr.insee.ddi.lifecycle33.reusable.ReferenceType;
import fr.insee.ddi.lifecycle33.reusable.TypeOfObjectType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDI4toDDI3ConverterServiceImpl implements DDI4toDDI3ConverterService {
    static final Logger logger = LoggerFactory.getLogger(DDI4toDDI3ConverterServiceImpl.class);

    private static final String DEFAULT_ITEM_FORMAT = "DC337820-AF3A-4C0B-82F9-CF02535CDE83";

    private static final String DDI_INSTANCE_NS = "ddi:instance:3_3";
    private static final String DDI_REUSABLE_NS = "ddi:reusable:3_3";
    private static final String DDI_PHYSICAL_INSTANCE_NS = "ddi:physicalinstance:3_3";
    private static final String DDI_LOGICAL_PRODUCT_NS = "ddi:logicalproduct:3_3";
    private static final String DDI_GROUP_NS = "ddi:group:3_3";
    private static final String DDI_STUDY_UNIT_NS = "ddi:studyunit:3_3";

    private final Map<String, String> itemTypes;
    /** Valeur du {@code VersionResponsibility} des items écrits, issue de {@code colectica.yml}. */
    private final String versionResponsibility;

    private final Ddi4ToLifecycle33 ddi4ToLifecycle33;

    public DDI4toDDI3ConverterServiceImpl(Map<String, String> itemTypes, String versionResponsibility) {
        this(itemTypes, versionResponsibility, new Ddi4ToLifecycle33(versionResponsibility));
    }

    DDI4toDDI3ConverterServiceImpl(
            Map<String, String> itemTypes, String versionResponsibility, Ddi4ToLifecycle33 ddi4ToLifecycle33) {
        this.itemTypes = itemTypes;
        this.versionResponsibility = versionResponsibility;
        this.ddi4ToLifecycle33 = ddi4ToLifecycle33;
    }

    private Ddi3Response.Ddi3Item createDdi3Item(
            String typeId, String agency, String version, String id, String xmlFragment, String versionDate) {
        return new Ddi3Response.Ddi3Item(
                typeId,
                agency,
                version,
                id,
                xmlFragment,
                versionDate,
                versionResponsibility,
                false,
                false,
                false,
                DEFAULT_ITEM_FORMAT);
    }

    private static String dateTimeOf(CogsDate versionDate) {
        return versionDate != null ? versionDate.dateTime() : null;
    }

    /**
     * Un item converti, gardé sous sa forme XmlBeans : la sérialisation en texte n'est faite que
     * pour l'écriture Colectica. Le document FragmentInstance les assemble tels quels — les
     * re-parser depuis leur texte coûtait un parseur SAX par item (19 s pour 45 000 catégories).
     */
    private record ConvertedFragment(
            String itemType,
            String agency,
            String version,
            String id,
            String versionDate,
            FragmentDocument document,
            String contentNamespace) {}

    private List<ConvertedFragment> convertFragments(Ddi4Response ddi4) {
        List<ConvertedFragment> fragments = new ArrayList<>();

        if (ddi4.physicalInstance() != null) {
            ddi4.physicalInstance()
                    .forEach(pi -> fragments.add(new ConvertedFragment(
                            itemTypes.get("PhysicalInstance"),
                            pi.agency(),
                            pi.version(),
                            pi.id(),
                            dateTimeOf(pi.versionDate()),
                            ddi4ToLifecycle33.toPhysicalInstance(pi),
                            DDI_PHYSICAL_INSTANCE_NS)));
        }
        if (ddi4.dataRelationship() != null) {
            ddi4.dataRelationship()
                    .forEach(dr -> fragments.add(new ConvertedFragment(
                            itemTypes.get("DataRelationship"),
                            dr.agency(),
                            dr.version(),
                            dr.id(),
                            dateTimeOf(dr.versionDate()),
                            ddi4ToLifecycle33.toDataRelationship(dr),
                            DDI_LOGICAL_PRODUCT_NS)));
        }
        if (ddi4.variable() != null) {
            ddi4.variable()
                    .forEach(var -> fragments.add(new ConvertedFragment(
                            itemTypes.get("Variable"),
                            var.agency(),
                            var.version(),
                            var.id(),
                            dateTimeOf(var.versionDate()),
                            ddi4ToLifecycle33.toVariable(var),
                            DDI_LOGICAL_PRODUCT_NS)));
        }
        if (ddi4.codeList() != null) {
            ddi4.codeList()
                    .forEach(cl -> fragments.add(new ConvertedFragment(
                            itemTypes.get("CodeList"),
                            cl.agency(),
                            cl.version(),
                            cl.id(),
                            dateTimeOf(cl.versionDate()),
                            ddi4ToLifecycle33.toCodeList(cl),
                            DDI_LOGICAL_PRODUCT_NS)));
        }
        if (ddi4.category() != null) {
            ddi4.category()
                    .forEach(cat -> fragments.add(new ConvertedFragment(
                            itemTypes.get("Category"),
                            cat.agency(),
                            cat.version(),
                            cat.id(),
                            dateTimeOf(cat.versionDate()),
                            ddi4ToLifecycle33.toCategory(cat),
                            DDI_LOGICAL_PRODUCT_NS)));
        }
        if (ddi4.managedMissingValuesRepresentation() != null) {
            ddi4.managedMissingValuesRepresentation()
                    .forEach(mmvr -> fragments.add(new ConvertedFragment(
                            itemTypes.get("ManagedMissingValuesRepresentation"),
                            mmvr.agency(),
                            mmvr.version(),
                            mmvr.id(),
                            dateTimeOf(mmvr.versionDate()),
                            ddi4ToLifecycle33.toManagedMissingValuesRepresentation(mmvr),
                            DDI_LOGICAL_PRODUCT_NS)));
        }
        return fragments;
    }

    @Override
    public Ddi3Response convertDdi4ToDdi3(Ddi4Response ddi4) {
        logger.info("Converting DDI4 to DDI3");

        List<Ddi3Response.Ddi3Item> items = convertFragments(ddi4).stream()
                .map(fragment -> createDdi3Item(
                        fragment.itemType(),
                        fragment.agency(),
                        fragment.version(),
                        fragment.id(),
                        fragment.document().xmlText(fragmentXmlOptions(fragment.contentNamespace())),
                        fragment.versionDate()))
                .toList();

        Ddi3Response.Ddi3Options options = new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace"));
        return new Ddi3Response(options, items);
    }

    @Override
    public Ddi3Response.Ddi3Item toCodeListSchemeItem(Ddi4CodeListScheme scheme) {
        String xmlFragment =
                ddi4ToLifecycle33.toCodeListScheme(scheme).xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
        return createDdi3Item(
                itemTypes.get("CodeListScheme"),
                scheme.agency(),
                scheme.version(),
                scheme.id(),
                xmlFragment,
                dateTimeOf(scheme.versionDate()));
    }

    @Override
    public Ddi3Response.Ddi3Item toCategorySchemeItem(Ddi4CategoryScheme scheme) {
        String xmlFragment =
                ddi4ToLifecycle33.toCategoryScheme(scheme).xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
        return createDdi3Item(
                itemTypes.get("CategoryScheme"),
                scheme.agency(),
                scheme.version(),
                scheme.id(),
                xmlFragment,
                dateTimeOf(scheme.versionDate()));
    }

    @Override
    public Ddi3Response.Ddi3Item toVariableSchemeItem(Ddi4VariableScheme scheme) {
        String xmlFragment =
                ddi4ToLifecycle33.toVariableScheme(scheme).xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
        return createDdi3Item(
                itemTypes.get("VariableScheme"),
                scheme.agency(),
                scheme.version(),
                scheme.id(),
                xmlFragment,
                dateTimeOf(scheme.versionDate()));
    }

    @Override
    public Ddi3Response.Ddi3Item toManagedRepresentationSchemeItem(Ddi4ManagedRepresentationScheme scheme) {
        String xmlFragment = ddi4ToLifecycle33
                .toManagedRepresentationScheme(scheme)
                .xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
        return createDdi3Item(
                itemTypes.get("ManagedRepresentationScheme"),
                scheme.agency(),
                scheme.version(),
                scheme.id(),
                xmlFragment,
                dateTimeOf(scheme.versionDate()));
    }

    @Override
    public Ddi3Response.Ddi3Item toManagedMissingValuesRepresentationItem(
            Ddi4ManagedMissingValuesRepresentation managedMissingValuesRepresentation) {
        String xmlFragment = ddi4ToLifecycle33
                .toManagedMissingValuesRepresentation(managedMissingValuesRepresentation)
                .xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
        return createDdi3Item(
                itemTypes.get("ManagedMissingValuesRepresentation"),
                managedMissingValuesRepresentation.agency(),
                managedMissingValuesRepresentation.version(),
                managedMissingValuesRepresentation.id(),
                xmlFragment,
                dateTimeOf(managedMissingValuesRepresentation.versionDate()));
    }

    @Override
    public Ddi3Response.Ddi3Item toCodeListItem(Ddi4CodeList codeList) {
        String xmlFragment = ddi4ToLifecycle33.toCodeList(codeList).xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
        return createDdi3Item(
                itemTypes.get("CodeList"),
                codeList.agency(),
                codeList.version(),
                codeList.id(),
                xmlFragment,
                dateTimeOf(codeList.versionDate()));
    }

    @Override
    public Ddi3Response.Ddi3Item toCategoryItem(Ddi4Category category) {
        String xmlFragment = ddi4ToLifecycle33.toCategory(category).xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
        return createDdi3Item(
                itemTypes.get("Category"),
                category.agency(),
                category.version(),
                category.id(),
                xmlFragment,
                dateTimeOf(category.versionDate()));
    }

    @Override
    public Ddi3Response.Ddi3Item toLogicalProductItem(Ddi4LogicalProduct logicalProduct) {
        String xmlFragment =
                ddi4ToLifecycle33.toLogicalProduct(logicalProduct).xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
        return createDdi3Item(
                itemTypes.get("LogicalProduct"),
                logicalProduct.agency(),
                logicalProduct.version(),
                logicalProduct.id(),
                xmlFragment,
                dateTimeOf(logicalProduct.versionDate()));
    }

    @Override
    public Ddi3Response.Ddi3Item toGroupItem(Ddi4Group group, String groupItemType) {
        String xmlFragment = ddi4ToLifecycle33.toGroup(group).xmlText(fragmentXmlOptions(DDI_GROUP_NS));
        return createDdi3Item(
                groupItemType,
                group.agency(),
                group.version(),
                group.id(),
                xmlFragment,
                dateTimeOf(group.versionDate()));
    }

    @Override
    public Ddi3Response.Ddi3Item toStudyUnitItem(Ddi4StudyUnit studyUnit, String studyUnitItemType) {
        String xmlFragment = ddi4ToLifecycle33.toStudyUnit(studyUnit).xmlText(fragmentXmlOptions(DDI_STUDY_UNIT_NS));
        return createDdi3Item(
                studyUnitItemType,
                studyUnit.agency(),
                studyUnit.version(),
                studyUnit.id(),
                xmlFragment,
                dateTimeOf(studyUnit.versionDate()));
    }

    @Override
    public String convertDdi4ToDdi3Xml(Ddi4Response ddi4) {
        logger.info("Converting DDI4 to DDI3 XML");
        Reference topLevelReference =
                (ddi4.topLevelReference() != null && !ddi4.topLevelReference().isEmpty())
                        ? ddi4.topLevelReference().get(0)
                        : null;
        return buildFragmentInstanceDocument(convertFragments(ddi4), topLevelReference);
    }

    private String buildFragmentInstanceDocument(List<ConvertedFragment> fragments, Reference topLevelReference) {
        if (fragments.isEmpty()) {
            throw new IllegalArgumentException("Ddi3Response must contain at least one item");
        }

        FragmentInstanceDocument doc = FragmentInstanceDocument.Factory.newInstance();
        var fiType = doc.addNewFragmentInstance();

        ConvertedFragment topLevelItem;
        String typeOfObject;
        if (topLevelReference != null) {
            final String tlrId = topLevelReference.id();
            final String tlrAgency = topLevelReference.agency();
            topLevelItem = fragments.stream()
                    .filter(item -> tlrId.equals(item.id()) && tlrAgency.equals(item.agency()))
                    .findFirst()
                    .orElse(fragments.getFirst());
            typeOfObject = topLevelReference.type();
        } else {
            topLevelItem = fragments.stream()
                    .filter(item -> itemTypes.get("PhysicalInstance").equals(item.itemType()))
                    .findFirst()
                    .orElse(fragments.getFirst());
            typeOfObject = getTypeOfObjectFromItemType(topLevelItem.itemType());
        }

        ReferenceType tlRef = fiType.addNewTopLevelReference();
        tlRef.addAgency(topLevelItem.agency());
        tlRef.addNewID().setStringValue(topLevelItem.id());
        tlRef.addVersion(topLevelItem.version());
        tlRef.setTypeOfObject(TypeOfObjectType.Enum.forString(typeOfObject));

        for (ConvertedFragment fragment : fragments) {
            fiType.addNewFragment().set(fragment.document().getFragment());
        }

        XmlOptions options = new XmlOptions();
        HashMap<String, String> prefixes = new HashMap<>();
        prefixes.put(DDI_INSTANCE_NS, "ddi");
        prefixes.put(DDI_REUSABLE_NS, "r");
        prefixes.put(DDI_LOGICAL_PRODUCT_NS, "l");
        prefixes.put(DDI_PHYSICAL_INSTANCE_NS, "pi");
        prefixes.put(DDI_GROUP_NS, "g");
        prefixes.put(DDI_STUDY_UNIT_NS, "s");
        options.setSaveSuggestedPrefixes(prefixes);
        options.setSaveAggressiveNamespaces();
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(2);

        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + doc.xmlText(options);
    }

    private String getTypeOfObjectFromItemType(String itemType) {
        return itemTypes.entrySet().stream()
                .filter(entry -> entry.getValue().equals(itemType))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("PhysicalInstance");
    }

    private static XmlOptions fragmentXmlOptions(String contentNs) {
        HashMap<String, String> prefixes = new HashMap<>();
        prefixes.put(DDI_INSTANCE_NS, "");
        prefixes.put(contentNs, "");
        prefixes.put(DDI_REUSABLE_NS, "r");
        XmlOptions options = new XmlOptions();
        options.setSaveSuggestedPrefixes(prefixes);
        return options;
    }
}
