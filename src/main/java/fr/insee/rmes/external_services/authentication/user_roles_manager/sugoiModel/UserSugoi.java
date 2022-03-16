package fr.insee.rmes.external_services.authentication.user_roles_manager.sugoiModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "lastName",
    "firstName",
    "mail",
    "username",
    "groups",
    "habilitations",
    "address",
    "metadatas",
    "attributes"
})
@Generated("jsonschema2pojo")
public class UserSugoi {

    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("mail")
    private String mail;
    @JsonProperty("username")
    private String username; //idep
    @JsonProperty("groups")
    private List<Group> groups = null;
    @JsonProperty("habilitations")
    private List<Object> habilitations = null;
    @JsonProperty("attributes")
    private Attributes attributes;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public UserSugoi() {
    }

    /**
     * 
     * @param lastName
     * @param firstName
     * @param address
     * @param mail
     * @param metadatas
     * @param habilitations
     * @param groups
     * @param attributes
     * @param username
     */
    public UserSugoi(String lastName, String firstName, String mail, String username, List<Group> groups, List<Object> habilitations, Attributes attributes) {
        super();
        this.lastName = lastName;
        this.firstName = firstName;
        this.mail = mail;
        this.username = username;
        this.groups = groups;
        this.habilitations = habilitations;
        this.attributes = attributes;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getCompleteName() {
    	return getFirstName()+" "+getLastName();
    }

    @JsonProperty("mail")
    public String getMail() {
        return mail;
    }

    @JsonProperty("mail")
    public void setMail(String mail) {
        this.mail = mail;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("groups")
    public List<Group> getGroups() {
        return groups;
    }

    @JsonProperty("groups")
    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @JsonProperty("habilitations")
    public List<Object> getHabilitations() {
        return habilitations;
    }

    @JsonProperty("habilitations")
    public void setHabilitations(List<Object> habilitations) {
        this.habilitations = habilitations;
    }

    @JsonProperty("attributes")
    public Attributes getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
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
        sb.append(UserSugoi.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("lastName");
        sb.append('=');
        sb.append(((this.lastName == null)?"<null>":this.lastName));
        sb.append(',');
        sb.append("firstName");
        sb.append('=');
        sb.append(((this.firstName == null)?"<null>":this.firstName));
        sb.append(',');
        sb.append("mail");
        sb.append('=');
        sb.append(((this.mail == null)?"<null>":this.mail));
        sb.append(',');
        sb.append("username");
        sb.append('=');
        sb.append(((this.username == null)?"<null>":this.username));
        sb.append(',');
        sb.append("groups");
        sb.append('=');
        sb.append(((this.groups == null)?"<null>":this.groups));
        sb.append(',');
        sb.append("habilitations");
        sb.append('=');
        sb.append(((this.habilitations == null)?"<null>":this.habilitations));
        sb.append(',');
        sb.append("attributes");
        sb.append('=');
        sb.append(((this.attributes == null)?"<null>":this.attributes));
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
		return Objects.hash(additionalProperties, attributes, firstName, groups, habilitations, lastName, mail,
				username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserSugoi other = (UserSugoi) obj;
		return Objects.equals(additionalProperties, other.additionalProperties)
				&& Objects.equals(attributes, other.attributes) && Objects.equals(firstName, other.firstName)
				&& Objects.equals(groups, other.groups) && Objects.equals(habilitations, other.habilitations)
				&& Objects.equals(lastName, other.lastName) && Objects.equals(mail, other.mail)
				&& Objects.equals(username, other.username);
	}


}

