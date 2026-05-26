package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DDI3toDDI4ConverterServiceImpl implements DDI3toDDI4ConverterService {
    static final Logger logger = LoggerFactory.getLogger(DDI3toDDI4ConverterServiceImpl.class);

    private final Map<String, String> itemTypes;
    private final Lifecycle33ToDdi4 lifecycle33ToDdi4;

    public DDI3toDDI4ConverterServiceImpl(Map<String, String> itemTypes) {
        this(itemTypes, new Lifecycle33ToDdi4());
    }

    DDI3toDDI4ConverterServiceImpl(Map<String, String> itemTypes, Lifecycle33ToDdi4 lifecycle33ToDdi4) {
        this.itemTypes = itemTypes;
        this.lifecycle33ToDdi4 = lifecycle33ToDdi4;
    }

    @Override
    public Ddi4Response convertDdi3ToDdi4(Ddi3Response ddi3, String schemaUrl) {
        logger.info("Converting DDI3 to DDI4");

        List<Ddi4PhysicalInstance> physicalInstances = new ArrayList<>();
        List<Ddi4DataRelationship> dataRelationships = new ArrayList<>();
        List<Ddi4Variable> variables = new ArrayList<>();
        List<Ddi4CodeList> codeLists = new ArrayList<>();
        List<Ddi4Category> categories = new ArrayList<>();
        List<Reference> topLevelReferences = new ArrayList<>();

        if (ddi3.items() != null) {
            for (Ddi3Response.Ddi3Item item : ddi3.items()) {
                try {
                    FragmentDocument fragment = FragmentDocument.Factory.parse(item.item());
                    String itemType = item.itemType();
                    if (itemTypes.get("PhysicalInstance").equals(itemType)) {
                        physicalInstances.add(lifecycle33ToDdi4.toPhysicalInstance(fragment));
                        topLevelReferences.add(Reference.of(
                            item.agencyId(),
                            item.identifier(),
                            item.version(),
                            "PhysicalInstance"
                        ));
                    } else if (itemTypes.get("DataRelationship").equals(itemType)) {
                        dataRelationships.add(lifecycle33ToDdi4.toDataRelationship(fragment));
                    } else if (itemTypes.get("Variable").equals(itemType)) {
                        variables.add(lifecycle33ToDdi4.toVariable(fragment));
                    } else if (itemTypes.get("CodeList").equals(itemType)) {
                        codeLists.add(lifecycle33ToDdi4.toCodeList(fragment));
                    } else if (itemTypes.get("Category").equals(itemType)) {
                        categories.add(lifecycle33ToDdi4.toCategory(fragment));
                    }
                } catch (XmlException e) {
                    logger.error("Error parsing DDI3 item of type {}", item.itemType(), e);
                    throw new RuntimeException("Error parsing DDI3 item", e);
                }
            }
        }

        return new Ddi4Response(
            schemaUrl,
            topLevelReferences.isEmpty() ? null : topLevelReferences,
            physicalInstances.isEmpty() ? null : physicalInstances,
            dataRelationships.isEmpty() ? null : dataRelationships,
            variables.isEmpty() ? null : variables,
            codeLists.isEmpty() ? null : codeLists,
            categories.isEmpty() ? null : categories
        );
    }
}
