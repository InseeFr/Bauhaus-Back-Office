package fr.insee.rmes.external_services.authentication.user_roles_manager.sugoiModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "phone_number",
    "common_name",
    "insee_timbre",
    "personal_title",
    "description",
    "insee_organisme",
    "insee_roles_applicatifs"
})
@Generated("jsonschema2pojo")
public class Attributes {

    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @JsonProperty("common_name")
    private String commonName;
    
    @JsonProperty("insee_timbre")
    private String inseeTimbre;
    
    @JsonProperty("personal_title")
    private String personalTitle;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("insee_organisme")
    private String inseeOrganisme;
    
    @JsonProperty("insee_roles_applicatifs")
    private List<String> inseeRolesApplicatifs = null;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Attributes() {
    }

    /**
     * 
     * @param commonName
     * @param inseeRolesApplicatifs
     * @param phoneNumber
     * @param inseeOrganisme
     * @param description
     * @param inseeTimbre
     * @param personalTitle
     */
    public Attributes(String phoneNumber, String commonName, String inseeTimbre, String personalTitle, String description, String inseeOrganisme, List<String> inseeRolesApplicatifs) {
        super();
        this.phoneNumber = phoneNumber;
        this.commonName = commonName;
        this.inseeTimbre = inseeTimbre;
        this.personalTitle = personalTitle;
        this.description = description;
        this.inseeOrganisme = inseeOrganisme;
        this.inseeRolesApplicatifs = inseeRolesApplicatifs;
    }

    @JsonProperty("phone_number")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @JsonProperty("phone_number")
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @JsonProperty("common_name")
    public String getCommonName() {
        return commonName;
    }

    @JsonProperty("common_name")
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    @JsonProperty("insee_timbre")
    public String getInseeTimbre() {
        return inseeTimbre;
    }

    @JsonProperty("insee_timbre")
    public void setInseeTimbre(String inseeTimbre) {
        this.inseeTimbre = inseeTimbre;
    }

    @JsonProperty("personal_title")
    public String getPersonalTitle() {
        return personalTitle;
    }

    @JsonProperty("personal_title")
    public void setPersonalTitle(String personalTitle) {
        this.personalTitle = personalTitle;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("insee_organisme")
    public String getInseeOrganisme() {
        return inseeOrganisme;
    }

    @JsonProperty("insee_organisme")
    public void setInseeOrganisme(String inseeOrganisme) {
        this.inseeOrganisme = inseeOrganisme;
    }

    @JsonProperty("insee_roles_applicatifs")
    public List<String> getInseeRolesApplicatifs() {
        return inseeRolesApplicatifs;
    }

    @JsonProperty("insee_roles_applicatifs")
    public void setInseeRolesApplicatifs(List<String> inseeRolesApplicatifs) {
        this.inseeRolesApplicatifs = inseeRolesApplicatifs;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Attributes.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("phoneNumber");
        sb.append('=');
        sb.append(((this.phoneNumber == null)?"<null>":this.phoneNumber));
        sb.append(',');
        sb.append("commonName");
        sb.append('=');
        sb.append(((this.commonName == null)?"<null>":this.commonName));
        sb.append(',');
        sb.append("inseeTimbre");
        sb.append('=');
        sb.append(((this.inseeTimbre == null)?"<null>":this.inseeTimbre));
        sb.append(',');
        sb.append("personalTitle");
        sb.append('=');
        sb.append(((this.personalTitle == null)?"<null>":this.personalTitle));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("inseeOrganisme");
        sb.append('=');
        sb.append(((this.inseeOrganisme == null)?"<null>":this.inseeOrganisme));
        sb.append(',');
        sb.append("inseeRolesApplicatifs");
        sb.append('=');
        sb.append(((this.inseeRolesApplicatifs == null)?"<null>":this.inseeRolesApplicatifs));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.commonName == null)? 0 :this.commonName.hashCode()));
        result = ((result* 31)+((this.inseeRolesApplicatifs == null)? 0 :this.inseeRolesApplicatifs.hashCode()));
        result = ((result* 31)+((this.phoneNumber == null)? 0 :this.phoneNumber.hashCode()));
        result = ((result* 31)+((this.inseeOrganisme == null)? 0 :this.inseeOrganisme.hashCode()));
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.inseeTimbre == null)? 0 :this.inseeTimbre.hashCode()));
        result = ((result* 31)+((this.personalTitle == null)? 0 :this.personalTitle.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Attributes) == false) {
            return false;
        }
        Attributes rhs = ((Attributes) other);
        return (((((((((this.commonName == rhs.commonName)||((this.commonName!= null)&&this.commonName.equals(rhs.commonName)))&&((this.inseeRolesApplicatifs == rhs.inseeRolesApplicatifs)||((this.inseeRolesApplicatifs!= null)&&this.inseeRolesApplicatifs.equals(rhs.inseeRolesApplicatifs))))&&((this.phoneNumber == rhs.phoneNumber)||((this.phoneNumber!= null)&&this.phoneNumber.equals(rhs.phoneNumber))))&&((this.inseeOrganisme == rhs.inseeOrganisme)||((this.inseeOrganisme!= null)&&this.inseeOrganisme.equals(rhs.inseeOrganisme))))&&((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.inseeTimbre == rhs.inseeTimbre)||((this.inseeTimbre!= null)&&this.inseeTimbre.equals(rhs.inseeTimbre))))&&((this.personalTitle == rhs.personalTitle)||((this.personalTitle!= null)&&this.personalTitle.equals(rhs.personalTitle))));
    }

}
