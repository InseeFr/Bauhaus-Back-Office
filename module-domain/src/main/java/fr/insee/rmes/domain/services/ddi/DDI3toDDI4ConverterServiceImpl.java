package fr.insee.rmes.domain.services.ddi;

import fr.insee.rmes.domain.model.ddi.*;
import fr.insee.rmes.domain.port.clientside.DDI3toDDI4ConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DDI3toDDI4ConverterServiceImpl implements DDI3toDDI4ConverterService {
    static final Logger logger = LoggerFactory.getLogger(DDI3toDDI4ConverterServiceImpl.class);

    // DDI 3.3 Item Type UUIDs
    private static final String PHYSICAL_INSTANCE_TYPE_ID = "a51e85bb-6259-4488-8df2-f08cb43485f8";
    private static final String DATA_RELATIONSHIP_TYPE_ID = "f39ff278-8500-45fe-a850-3906da2d242b";
    private static final String VARIABLE_TYPE_ID = "683889c6-f74b-4d5e-92ed-908c0a42bb2d";
    private static final String CODE_LIST_TYPE_ID = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";
    private static final String CATEGORY_TYPE_ID = "7e47c269-bcab-40f7-a778-af7bbc4e3d00";

    private final Ddi3XmlReader xmlReader;

    public DDI3toDDI4ConverterServiceImpl() {
        this.xmlReader = new Ddi3XmlReader();
    }

    @Override
    public Ddi4Response convertDdi3ToDdi4(Ddi3Response ddi3) {
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
                    switch (item.itemType()) {
                        case PHYSICAL_INSTANCE_TYPE_ID -> {
                            Ddi4PhysicalInstance pi = xmlReader.parsePhysicalInstance(item.item());
                            physicalInstances.add(pi);
                            topLevelReferences.add(new TopLevelReference(
                                item.agencyId(),
                                item.identifier(),
                                item.version(),
                                "PhysicalInstance"
                            ));
                        }
                        case DATA_RELATIONSHIP_TYPE_ID -> {
                            dataRelationships.add(xmlReader.parseDataRelationship(item.item()));
                        }
                        case VARIABLE_TYPE_ID -> {
                            variables.add(xmlReader.parseVariable(item.item()));
                        }
                        case CODE_LIST_TYPE_ID -> {
                            codeLists.add(xmlReader.parseCodeList(item.item()));
                        }
                        case CATEGORY_TYPE_ID -> {
                            categories.add(xmlReader.parseCategory(item.item()));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error parsing DDI3 item of type {}", item.itemType(), e);
                    throw new RuntimeException("Error parsing DDI3 item", e);
                }
            }
        }

        return new Ddi4Response(
            "file:/jsonSchema.json",
            topLevelReferences.isEmpty() ? null : topLevelReferences,
            physicalInstances.isEmpty() ? null : physicalInstances,
            dataRelationships.isEmpty() ? null : dataRelationships,
            variables.isEmpty() ? null : variables,
            codeLists.isEmpty() ? null : codeLists,
            categories.isEmpty() ? null : categories
        );
    }
}