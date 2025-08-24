package fr.insee.rmes.model.structures;

import fr.insee.rmes.domain.exceptions.RmesException;

public class ComponentDefinition {

    private String id;
    private String created;
    private String modified;
    private String order;
    private String[] attachment = new String[0];
    private Boolean required = false;
    private String notation;
    private String labelLg1;
    private String labelLg2;

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

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public String getLabelLg1() {
        return labelLg1;
    }

    public void setLabelLg1(String labelLg1) {
        this.labelLg1 = labelLg1;
    }

    public String getLabelLg2() {
        return labelLg2;
    }

    public void setLabelLg2(String labelLg2) {
        this.labelLg2 = labelLg2;
    }
}
