package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

public class DDI4toDDI3ConverterServiceImpl implements DDI4toDDI3ConverterService {
    static final Logger logger = LoggerFactory.getLogger(DDI4toDDI3ConverterServiceImpl.class);

    // DDI 3.3 Item Type UUIDs
    private static final String PHYSICAL_INSTANCE_TYPE_ID = "a51e85bb-6259-4488-8df2-f08cb43485f8";
    private static final String DATA_RELATIONSHIP_TYPE_ID = "f39ff278-8500-45fe-a850-3906da2d242b";
    private static final String VARIABLE_TYPE_ID = "683889c6-f74b-4d5e-92ed-908c0a42bb2d";
    private static final String CODE_LIST_TYPE_ID = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";
    private static final String CATEGORY_TYPE_ID = "7e47c269-bcab-40f7-a778-af7bbc4e3d00";

    private static final String DEFAULT_VERSION_RESPONSIBILITY = "abcde";
    private static final String DEFAULT_ITEM_FORMAT = "DC337820-AF3A-4C0B-82F9-CF02535CDE83";

    private final Ddi3XmlWriter xmlWriter;

    public DDI4toDDI3ConverterServiceImpl() {
        this.xmlWriter = new Ddi3XmlWriter();
    }

    /**
     * Helper method to create a DDI3 item with default values
     */
    private Ddi3Response.Ddi3Item createDdi3Item(String typeId, String agency, String version,
                                                 String id, String xmlFragment, String versionDate) {
        return new Ddi3Response.Ddi3Item(
                typeId,
                agency,
                version,
                id,
                xmlFragment,
                versionDate,
                DEFAULT_VERSION_RESPONSIBILITY,
                false,
                false,
                false,
                DEFAULT_ITEM_FORMAT
        );
    }

    @Override
    public Ddi3Response convertDdi4ToDdi3(Ddi4Response ddi4) {
        logger.info("Converting DDI4 to DDI3");

        List<Ddi3Response.Ddi3Item> items = new ArrayList<>();

        // Convert PhysicalInstances
        if (ddi4.physicalInstance() != null) {
            ddi4.physicalInstance().forEach(pi -> {
                try {
                    String xmlFragment = xmlWriter.buildPhysicalInstanceXml(pi);
                    items.add(createDdi3Item(PHYSICAL_INSTANCE_TYPE_ID, pi.agency(), pi.version(),
                            pi.id(), xmlFragment, pi.versionDate()));
                } catch (XMLStreamException e) {
                    logger.error("Error converting PhysicalInstance to XML", e);
                    throw new RuntimeException("Error converting PhysicalInstance to XML", e);
                }
            });
        }

        // Convert DataRelationships
        if (ddi4.dataRelationship() != null) {
            ddi4.dataRelationship().forEach(dr -> {
                try {
                    String xmlFragment = xmlWriter.buildDataRelationshipXml(dr);
                    items.add(createDdi3Item(DATA_RELATIONSHIP_TYPE_ID, dr.agency(), dr.version(),
                            dr.id(), xmlFragment, dr.versionDate()));
                } catch (XMLStreamException e) {
                    logger.error("Error converting DataRelationship to XML", e);
                    throw new RuntimeException("Error converting DataRelationship to XML", e);
                }
            });
        }

        // Convert Variables
        if (ddi4.variable() != null) {
            ddi4.variable().forEach(var -> {
                try {
                    String xmlFragment = xmlWriter.buildVariableXml(var);
                    items.add(createDdi3Item(VARIABLE_TYPE_ID, var.agency(), var.version(),
                            var.id(), xmlFragment, var.versionDate()));
                } catch (XMLStreamException e) {
                    logger.error("Error converting Variable to XML", e);
                    throw new RuntimeException("Error converting Variable to XML", e);
                }
            });
        }

        // Convert CodeLists
        if (ddi4.codeList() != null) {
            ddi4.codeList().forEach(cl -> {
                try {
                    String xmlFragment = xmlWriter.buildCodeListXml(cl);
                    items.add(createDdi3Item(CODE_LIST_TYPE_ID, cl.agency(), cl.version(),
                            cl.id(), xmlFragment, cl.versionDate()));
                } catch (XMLStreamException e) {
                    logger.error("Error converting CodeList to XML", e);
                    throw new RuntimeException("Error converting CodeList to XML", e);
                }
            });
        }

        // Convert Categories
        if (ddi4.category() != null) {
            ddi4.category().forEach(cat -> {
                try {
                    String xmlFragment = xmlWriter.buildCategoryXml(cat);
                    items.add(createDdi3Item(CATEGORY_TYPE_ID, cat.agency(), cat.version(),
                            cat.id(), xmlFragment, cat.versionDate()));
                } catch (XMLStreamException e) {
                    logger.error("Error converting Category to XML", e);
                    throw new RuntimeException("Error converting Category to XML", e);
                }
            });
        }

        Ddi3Response.Ddi3Options options = new Ddi3Response.Ddi3Options(
            List.of("RegisterOrReplace")
        );

        return new Ddi3Response(options, items);
    }
}