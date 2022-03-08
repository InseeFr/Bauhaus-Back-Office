
package fr.insee.rmes.external_services.authentication.stamps.apiRHModel;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "numeroVoie",
    "extension",
    "nomVoie",
    "complementAdresse",
    "bureauDistribution",
    "commune",
    "codePostal",
    "departement",
    "departementLibelle",
    "dateDebut",
    "dateFin"
})
@Generated("jsonschema2pojo")
public class AdresseFrance {

    @JsonProperty("numeroVoie")
    private String numeroVoie;
    @JsonProperty("extension")
    private Object extension;
    @JsonProperty("nomVoie")
    private String nomVoie;
    @JsonProperty("complementAdresse")
    private String complementAdresse;
    @JsonProperty("bureauDistribution")
    private String bureauDistribution;
    @JsonProperty("commune")
    private String commune;
    @JsonProperty("codePostal")
    private String codePostal;
    @JsonProperty("departement")
    private String departement;
    @JsonProperty("departementLibelle")
    private String departementLibelle;
    @JsonProperty("dateDebut")
    private String dateDebut;
    @JsonProperty("dateFin")
    private Object dateFin;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public AdresseFrance() {
    }

    /**
     * 
     * @param nomVoie
     * @param bureauDistribution
     * @param extension
     * @param complementAdresse
     * @param departement
     * @param departementLibelle
     * @param dateDebut
     * @param commune
     * @param numeroVoie
     * @param codePostal
     * @param dateFin
     */
    public AdresseFrance(String numeroVoie, Object extension, String nomVoie, String complementAdresse, String bureauDistribution, String commune, String codePostal, String departement, String departementLibelle, String dateDebut, Object dateFin) {
        super();
        this.numeroVoie = numeroVoie;
        this.extension = extension;
        this.nomVoie = nomVoie;
        this.complementAdresse = complementAdresse;
        this.bureauDistribution = bureauDistribution;
        this.commune = commune;
        this.codePostal = codePostal;
        this.departement = departement;
        this.departementLibelle = departementLibelle;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    @JsonProperty("numeroVoie")
    public String getNumeroVoie() {
        return numeroVoie;
    }

    @JsonProperty("numeroVoie")
    public void setNumeroVoie(String numeroVoie) {
        this.numeroVoie = numeroVoie;
    }

    public AdresseFrance withNumeroVoie(String numeroVoie) {
        this.numeroVoie = numeroVoie;
        return this;
    }

    @JsonProperty("extension")
    public Object getExtension() {
        return extension;
    }

    @JsonProperty("extension")
    public void setExtension(Object extension) {
        this.extension = extension;
    }

    public AdresseFrance withExtension(Object extension) {
        this.extension = extension;
        return this;
    }

    @JsonProperty("nomVoie")
    public String getNomVoie() {
        return nomVoie;
    }

    @JsonProperty("nomVoie")
    public void setNomVoie(String nomVoie) {
        this.nomVoie = nomVoie;
    }

    public AdresseFrance withNomVoie(String nomVoie) {
        this.nomVoie = nomVoie;
        return this;
    }

    @JsonProperty("complementAdresse")
    public String getComplementAdresse() {
        return complementAdresse;
    }

    @JsonProperty("complementAdresse")
    public void setComplementAdresse(String complementAdresse) {
        this.complementAdresse = complementAdresse;
    }

    public AdresseFrance withComplementAdresse(String complementAdresse) {
        this.complementAdresse = complementAdresse;
        return this;
    }

    @JsonProperty("bureauDistribution")
    public String getBureauDistribution() {
        return bureauDistribution;
    }

    @JsonProperty("bureauDistribution")
    public void setBureauDistribution(String bureauDistribution) {
        this.bureauDistribution = bureauDistribution;
    }

    public AdresseFrance withBureauDistribution(String bureauDistribution) {
        this.bureauDistribution = bureauDistribution;
        return this;
    }

    @JsonProperty("commune")
    public String getCommune() {
        return commune;
    }

    @JsonProperty("commune")
    public void setCommune(String commune) {
        this.commune = commune;
    }

    public AdresseFrance withCommune(String commune) {
        this.commune = commune;
        return this;
    }

    @JsonProperty("codePostal")
    public String getCodePostal() {
        return codePostal;
    }

    @JsonProperty("codePostal")
    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public AdresseFrance withCodePostal(String codePostal) {
        this.codePostal = codePostal;
        return this;
    }

    @JsonProperty("departement")
    public String getDepartement() {
        return departement;
    }

    @JsonProperty("departement")
    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public AdresseFrance withDepartement(String departement) {
        this.departement = departement;
        return this;
    }

    @JsonProperty("departementLibelle")
    public String getDepartementLibelle() {
        return departementLibelle;
    }

    @JsonProperty("departementLibelle")
    public void setDepartementLibelle(String departementLibelle) {
        this.departementLibelle = departementLibelle;
    }

    public AdresseFrance withDepartementLibelle(String departementLibelle) {
        this.departementLibelle = departementLibelle;
        return this;
    }

    @JsonProperty("dateDebut")
    public String getDateDebut() {
        return dateDebut;
    }

    @JsonProperty("dateDebut")
    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public AdresseFrance withDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
        return this;
    }

    @JsonProperty("dateFin")
    public Object getDateFin() {
        return dateFin;
    }

    @JsonProperty("dateFin")
    public void setDateFin(Object dateFin) {
        this.dateFin = dateFin;
    }

    public AdresseFrance withDateFin(Object dateFin) {
        this.dateFin = dateFin;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public AdresseFrance withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
