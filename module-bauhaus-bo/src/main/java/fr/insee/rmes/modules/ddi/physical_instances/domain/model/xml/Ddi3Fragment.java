package fr.insee.rmes.modules.ddi.physical_instances.domain.model.xml;

import jakarta.xml.bind.annotation.*;

/**
 * Generic wrapper for DDI3 XML fragments
 */
@XmlRootElement(name = "Fragment", namespace = "ddi:instance:3_3")
@XmlAccessorType(XmlAccessType.FIELD)
public class Ddi3Fragment {

    @XmlAnyElement(lax = true)
    private Object content;

    public Ddi3Fragment() {
    }

    public Ddi3Fragment(Object content) {
        this.content = content;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
