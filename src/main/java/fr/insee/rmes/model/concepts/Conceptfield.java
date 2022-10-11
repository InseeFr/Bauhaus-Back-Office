package fr.insee.rmes.model.concepts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "altLabelLg1",
        "altLabelLg2",
        "creator",
        "contributor",
        "disseminationStatus",
        "additionalMaterial",
        "created",
        "modified",
        "valid",
        "conceptVersion",
        "isValidated",
        "defcourteLg1",
        "defcourteLg2"
})
public class  Conceptfield  {

    @JsonProperty("id")
    private String id;
    @JsonProperty("prefLabelLg1")
    private String prefLabelLg1;
    @JsonProperty("prefLabelLg2")
    private String prefLabelLg2;
    @JsonProperty("altLabelLg1")
    private String altLabelLg1;
    @JsonProperty("altLabelLg2")
    private String altLabelLg2;
    @JsonProperty("creator")
    private String creator;
    @JsonProperty("contributor")
    private String contributor;
    @JsonProperty("disseminationStatus")
    private String disseminationStatus;
    @JsonProperty("additionalMaterial")
    private String additionalMaterial;
    @JsonProperty("created")
    private String created;
    @JsonProperty("modified")
    private String modified;
    @JsonProperty("valid")
    private String valid;
    @JsonProperty("conceptVersion")
    private String conceptVersion;
    @JsonProperty("isValidated")
    private String isValidated;
    @JsonProperty("defcourteLg1")
    private String defcourteLg1;
    @JsonProperty("defcourteLg2")
    private String defcourteLg2;

    public Conceptfield  () {}


    public Conceptfield  (String id, String prefLabelLg1, String creator,
                         String contributor,String disseminationStatus,String additionalMaterial,
                         String created,String modified,String valid,String conceptVersion,String isValidated,String defcourteLg1
                         ) {
        this.id=id;
        this.prefLabelLg1=prefLabelLg1;
        this.creator=creator;
        this.contributor=contributor;
        this.disseminationStatus=disseminationStatus;
        this.additionalMaterial=additionalMaterial;
        this.created=created;
        this.modified=modified;
        this.valid=valid;
        this.conceptVersion=conceptVersion;
        this.isValidated=isValidated;
        this.defcourteLg1=defcourteLg1;

    }

    private Conceptfield(UserBuilder userBuilder) {
        this.id=userBuilder.id;
        this.prefLabelLg2=userBuilder.prefLabelLg2;
        this.creator=userBuilder.creator;
        this.contributor=userBuilder.contributor;
        this.disseminationStatus=userBuilder.disseminationStatus;
        this.additionalMaterial=userBuilder.additionalMaterial;
        this.created=userBuilder.created;
        this.modified=userBuilder.modified;
        this.valid=userBuilder.valid;
        this.conceptVersion=userBuilder.conceptVersion;
        this.isValidated=userBuilder.isValidated;
        this.defcourteLg2=userBuilder.defcourteLg2;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAltLabelLg1() {
        return altLabelLg1;
    }

    public void setAltLabelLg1(String altLabelLg1) {
        this.altLabelLg1 = altLabelLg1;
    }

    public String getAltLabelLg2() {
        return altLabelLg2;
    }

    public void setAltLabelLg2(String altLabelLg2) {
        this.altLabelLg2 = altLabelLg2;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
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

    public String getIsValidated() {
        return isValidated;
    }

    public void setIsValidated(String isValidated) {
        this.isValidated = isValidated;
    }

    public String getDefcourteLg1() {
        return defcourteLg1;
    }

    public void setDefcourteLg1(String defcourteLg1) {
        this.defcourteLg1 = defcourteLg1;
    }

    public String getDefcourteLg2() {
        return defcourteLg2;
    }

    public void setDefcourteLg2(String defcourteLg2) {
        this.defcourteLg2 = defcourteLg2;
    }

    public String getPrefLabelLg1() {
        return prefLabelLg1;
    }

    public void setPrefLabelLg1(String prefLabelLg1) {
        this.prefLabelLg1 = prefLabelLg1;
    }

    public String getPrefLabelLg2() {
        return prefLabelLg2;
    }

    public void setPrefLabelLg2(String prefLabelLg2) {
        this.prefLabelLg2 = prefLabelLg2;
    }

    public static class UserBuilder {
        // required parameters
        private String id;
        private String prefLabelLg2;
        private String creator;
        private String contributor;
        private String disseminationStatus;
        private String additionalMaterial;
        private String created;
        private String modified;
        private String valid;
        private String conceptVersion;
        private String isValidated;
        private String defcourteLg2;

        public UserBuilder (String id) {
            this.id = id;
                }

        public UserBuilder setPrefLabelLg2(String prefLabelLg2) {
            this.prefLabelLg2 = prefLabelLg2;
            return this;
                    }

        public UserBuilder setCreator(String creator) {
            this.creator = creator;
            return this;
        }

        public UserBuilder setContributor(String contributor) {
            this.contributor = contributor;
            return this;
        }

        public UserBuilder setDisseminationStatus(String disseminationStatus) {
            this.disseminationStatus = disseminationStatus;
            return this;
        }

        public UserBuilder setAdditionalMaterial(String additionalMaterial) {
            this.additionalMaterial = additionalMaterial;
            return this;
        }

        public UserBuilder setCreated(String created) {
            this.created = created;
            return this;
        }

        public UserBuilder setModified(String modified) {
            this.modified = modified;
            return this;
        }

        public UserBuilder setValid(String valid) {
            this.valid = valid;
            return this;
        }

        public UserBuilder setConceptVersion(String conceptVersion) {
            this.conceptVersion = conceptVersion;
            return this;
        }

        public UserBuilder setIsValidated(String isValidated) {
            this.isValidated = isValidated;
            return this;
        }

        public UserBuilder setDefcourteLg2(String defcourteLg2) {
            this.defcourteLg2 = defcourteLg2;
            return this;
        }

        public Conceptfield build() {
            return new Conceptfield(this);
        }

    }
}
