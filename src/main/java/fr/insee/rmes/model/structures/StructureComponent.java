package fr.insee.rmes.model.structures;

import fr.insee.rmes.exceptions.RmesException;

public class StructureComponent {

    private String identifiant;
    private String id;
    private String labelLg1;
    private String labelLg2;
    private String type;
    private String concept;
    private String codeList;
    private String range;
    private DSD[] structures;

    public StructureComponent() throws RmesException {
        //nothing to do
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getCodeList() {
        return codeList;
    }

    public void setCodeList(String codeList) {
        this.codeList = codeList;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public DSD[] getStructures() {
        return structures;
    }

    public void setStructures(DSD[] structures) {
        this.structures = structures;
    }
}
