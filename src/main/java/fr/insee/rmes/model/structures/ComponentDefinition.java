package fr.insee.rmes.model.structures;

import fr.insee.rmes.exceptions.RmesException;

import java.util.ArrayList;
import java.util.Arrays;

public class ComponentDefinition {

    private String id;
    private String created;
    private String modified;
    private String order;
    private String[] attachment = new String[0];
    private Boolean required = false;

    private MutualizedComponent component;

    public ComponentDefinition() throws RmesException {
        //nothing to do
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String[] getAttachment() {
        return attachment;
    }

    public void setAttachment(String[] attachment) {
        this.attachment = attachment;
    }

    public MutualizedComponent getComponent() {
        return component;
    }

    public void setComponent(MutualizedComponent component) {
        this.component = component;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
