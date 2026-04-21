package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DDI3toDDI4ConverterServiceImpl implements
        DDI3toDDI4ConverterService {
    static final Logger logger = LoggerFactory.getLogger(DDI3toDDI4ConverterServiceImpl.class);

    private final Map<String, String> itemTypes;
    private final Ddi3XmlReader xmlReader;

    public DDI3toDDI4ConverterServiceImpl(Map<String, String> itemTypes) {
        this.itemTypes = itemTypes;
        this.xmlReader = new Ddi3XmlReader();
    }

    @Override
    public Ddi4Response convertDdi3ToDdi4(Ddi3Response ddi3, String schemaUrl) {
        logger.info("Converting DDI3 to DDI4");

        List<Ddi4PhysicalInstance> physicalInstances = new ArrayList<>();
        List<Ddi4DataRelationship> dataRelationships = new ArrayList<>();
        List<Ddi4Variable> variables = new ArrayList<>();
        List<Ddi4CodeList> codeLists = new ArrayList<>();
        List<Ddi4Category> categories = new ArrayList<>();
        List<TopLevelReference> topLevelReferences = new ArrayList<>();

        if (ddi3.items() != null) {
            for (Ddi3Response.Ddi3Item item : ddi3.items()) {
                try {
                    String itemType = item.itemType();
                    if (itemTypes.get("PhysicalInstance").equals(itemType)) {
                        Ddi4PhysicalInstance pi = xmlReader.parsePhysicalInstance(item.item());
                        physicalInstances.add(pi);
                        topLevelReferences.add(new TopLevelReference(
                            item.agencyId(),
                            item.identifier(),
                            item.version(),
                            "PhysicalInstance"
                        ));
                    } else if (itemTypes.get("DataRelationship").equals(itemType)) {
                        dataRelationships.add(xmlReader.parseDataRelationship(item.item()));
                    } else if (itemTypes.get("Variable").equals(itemType)) {
                        variables.add(xmlReader.parseVariable(item.item()));
                    } else if (itemTypes.get("CodeList").equals(itemType)) {
                        codeLists.add(xmlReader.parseCodeList(item.item()));
                    } else if (itemTypes.get("Category").equals(itemType)) {
                        categories.add(xmlReader.parseCategory(item.item()));
                    }
                } catch (Exception e) {
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