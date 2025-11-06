@XmlSchema(
    namespace = "ddi:instance:3_3",
    xmlns = {
        @XmlNs(prefix = "", namespaceURI = "ddi:instance:3_3"),
        @XmlNs(prefix = "r", namespaceURI = "ddi:reusable:3_3"),
        @XmlNs(prefix = "pi", namespaceURI = "ddi:physicalinstance:3_3"),
        @XmlNs(prefix = "lp", namespaceURI = "ddi:logicalproduct:3_3")
    },
    elementFormDefault = XmlNsForm.QUALIFIED
)
package fr.insee.rmes.modules.ddi.physical_instances.domain.model.xml;

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
