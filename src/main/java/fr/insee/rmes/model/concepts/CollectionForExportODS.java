package fr.insee.rmes.model.concepts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class CollectionForExportODS {


        //GENERAL
        private String id;//
        private String prefLabelLg1;//
        private String prefLabelLg2;//
        private String creator;//
        private String contributor;//

    private String disseminationStatus;
    private String additionalMaterial;
    private String valid;
    private String conceptVersion;

        //DATE
        private String created;//
        private String modified;//

        //STATUS
        private String isValidated;//

        //NOTES

        private String descriptionLg1;

        private String descriptionLg2;

        public String getCreated() {
            return created;
        }

        public String getModified() {
            return modified;
        }

        public String getIsValidated() {
            return isValidated;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPrefLabelLg1() {
            return prefLabelLg1;
        }

        public String getPrefLabelLg2() {
            return prefLabelLg2;
        }

        public String getCreator() {
            return creator;
        }

        public String getContributor() {
            return contributor;
        }

        public void setPrefLabelLg1(String prefLabelLg1) {
            this.prefLabelLg1 = prefLabelLg1;
        }

        public void setPrefLabelLg2(String prefLabelLg2) {
            this.prefLabelLg2 = prefLabelLg2;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public void setContributor(String contributor) {
            this.contributor = contributor;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public void setModified(String modified) {
            this.modified = modified;
        }

        public void setIsValidated(String isValidated) {
            this.isValidated = isValidated;
        }



    public String getDescriptionLg1() {
            return descriptionLg1;
        }



        public void setDescriptionLg1(String descriptionLg1) {
            this.descriptionLg1 = descriptionLg1;
        }



        public String getDescriptionLg2() {
            return descriptionLg2;
        }



        public void setDescriptionLg2(String descriptionLg2) {
            this.descriptionLg2 = descriptionLg2;
        }

    public String getDisseminationStatus() {
        return disseminationStatus;
    }

    public void setDisseminationStatus(String disseminationStatus) {
        this.disseminationStatus = disseminationStatus;
    }

    public String getAdditionalMaterial() {
        return additionalMaterial;
    }

    public void setAdditionalMaterial(String additionalMaterial) {
        this.additionalMaterial = additionalMaterial;
    }

    public String getValid() {
        return valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public String getConceptVersion() {
        return conceptVersion;
    }

    public void setConceptVersion(String conceptVersion) {
        this.conceptVersion = conceptVersion;
    }
}

