package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DDI4toDDI3ConverterServiceImpl implements DDI4toDDI3ConverterService {
    static final Logger logger = LoggerFactory.getLogger(DDI4toDDI3ConverterServiceImpl.class);

    private static final String DEFAULT_VERSION_RESPONSIBILITY = "abcde";
    private static final String DEFAULT_ITEM_FORMAT = "DC337820-AF3A-4C0B-82F9-CF02535CDE83";

    private final Map<String, String> itemTypes;
    private final Ddi3XmlWriter xmlWriter;

    public DDI4toDDI3ConverterServiceImpl(Map<String, String> itemTypes) {
        this.itemTypes = itemTypes;
        this.xmlWriter = new Ddi3XmlWriter(itemTypes);
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
                    items.add(createDdi3Item(itemTypes.get("PhysicalInstance"), pi.agency(), pi.version(),
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
                    items.add(createDdi3Item(itemTypes.get("DataRelationship"), dr.agency(), dr.version(),
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
                    items.add(createDdi3Item(itemTypes.get("Variable"), var.agency(), var.version(),
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
                    items.add(createDdi3Item(itemTypes.get("CodeList"), cl.agency(), cl.version(),
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
                    items.add(createDdi3Item(itemTypes.get("Category"), cat.agency(), cat.version(),
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

    @Override
    public String convertDdi4ToDdi3Xml(Ddi4Response ddi4) {
        logger.info("Converting DDI4 to DDI3 XML");

        // First convert to Ddi3Response using the existing method
        Ddi3Response ddi3Response = convertDdi4ToDdi3(ddi4);

        // Extract the first topLevelReference if present
        var topLevelReference = (ddi4.topLevelReference() != null && !ddi4.topLevelReference().isEmpty())
                ? ddi4.topLevelReference().get(0)
                : null;

        // Then build the complete FragmentInstance XML document
        return xmlWriter.buildFragmentInstanceDocument(ddi3Response, topLevelReference);
    }
}