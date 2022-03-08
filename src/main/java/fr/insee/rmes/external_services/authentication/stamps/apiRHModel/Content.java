
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
    "nudoss",
    "code",
    "identifiantGIP",
    "timbreTechnique",
    "timbre",
    "etablissement",
    "libelleCourt",
    "libelleLong",
    "niveau",
    "uniteMere",
    "agentResponsable",
    "uniteEvaluation",
    "dateDebut",
    "dateFin",
    "dateEffetLibelle",
    "region",
    "uniteInsee",
    "estAdministrative",
    "ssm",
    "frhl",
    "adresseFrance",
    "adresseEtranger",
    "estActive"
})
@Generated("jsonschema2pojo")
public class Content {

    @JsonProperty("nudoss")
    private Integer nudoss;
    @JsonProperty("code")
    private String code;
    @JsonProperty("identifiantGIP")
    private String identifiantGIP;
    @JsonProperty("timbreTechnique")
    private String timbreTechnique;
    @JsonProperty("timbre")
    private String timbre;
    @JsonProperty("etablissement")
    private Object etablissement;
    @JsonProperty("libelleCourt")
    private String libelleCourt;
    @JsonProperty("libelleLong")
    private String libelleLong;
    @JsonProperty("niveau")
    private String niveau;
    @JsonProperty("uniteMere")
    private String uniteMere;
    @JsonProperty("agentResponsable")
    private String agentResponsable;
    @JsonProperty("uniteEvaluation")
    private String uniteEvaluation;
    @JsonProperty("dateDebut")
    private String dateDebut;
    @JsonProperty("dateFin")
    private Object dateFin;
    @JsonProperty("dateEffetLibelle")
    private String dateEffetLibelle;
    @JsonProperty("region")
    private String region;
    @JsonProperty("uniteInsee")
    private Boolean uniteInsee;
    @JsonProperty("estAdministrative")
    private Boolean estAdministrative;
    @JsonProperty("ssm")
    private Object ssm;
    @JsonProperty("frhl")
    private String frhl;
    @JsonProperty("adresseFrance")
    @Valid
    private AdresseFrance adresseFrance;
    @JsonProperty("adresseEtranger")
    private Object adresseEtranger;
    @JsonProperty("estActive")
    private Boolean estActive;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Content() {
    }

    /**
     * 
     * @param frhl
     * @param libelleLong
     * @param estAdministrative
     * @param estActive
     * @param code
     * @param nudoss
     * @param identifiantGIP
     * @param etablissement
     * @param dateEffetLibelle
     * @param ssm
     * @param niveau
     * @param uniteMere
     * @param timbre
     * @param uniteEvaluation
     * @param dateDebut
     * @param uniteInsee
     * @param adresseEtranger
     * @param adresseFrance
     * @param timbreTechnique
     * @param agentResponsable
     * @param dateFin
     * @param region
     * @param libelleCourt
     */
    public Content(Integer nudoss, String code, String identifiantGIP, String timbreTechnique, String timbre, Object etablissement, String libelleCourt, String libelleLong, String niveau, String uniteMere, String agentResponsable, String uniteEvaluation, String dateDebut, Object dateFin, String dateEffetLibelle, String region, Boolean uniteInsee, Boolean estAdministrative, Object ssm, String frhl, AdresseFrance adresseFrance, Object adresseEtranger, Boolean estActive) {
        super();
        this.nudoss = nudoss;
        this.code = code;
        this.identifiantGIP = identifiantGIP;
        this.timbreTechnique = timbreTechnique;
        this.timbre = timbre;
        this.etablissement = etablissement;
        this.libelleCourt = libelleCourt;
        this.libelleLong = libelleLong;
        this.niveau = niveau;
        this.uniteMere = uniteMere;
        this.agentResponsable = agentResponsable;
        this.uniteEvaluation = uniteEvaluation;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.dateEffetLibelle = dateEffetLibelle;
        this.region = region;
        this.uniteInsee = uniteInsee;
        this.estAdministrative = estAdministrative;
        this.ssm = ssm;
        this.frhl = frhl;
        this.adresseFrance = adresseFrance;
        this.adresseEtranger = adresseEtranger;
        this.estActive = estActive;
    }

    @JsonProperty("nudoss")
    public Integer getNudoss() {
        return nudoss;
    }

    @JsonProperty("nudoss")
    public void setNudoss(Integer nudoss) {
        this.nudoss = nudoss;
    }

    public Content withNudoss(Integer nudoss) {
        this.nudoss = nudoss;
        return this;
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    public Content withCode(String code) {
        this.code = code;
        return this;
    }

    @JsonProperty("identifiantGIP")
    public String getIdentifiantGIP() {
        return identifiantGIP;
    }

    @JsonProperty("identifiantGIP")
    public void setIdentifiantGIP(String identifiantGIP) {
        this.identifiantGIP = identifiantGIP;
    }

    public Content withIdentifiantGIP(String identifiantGIP) {
        this.identifiantGIP = identifiantGIP;
        return this;
    }

    @JsonProperty("timbreTechnique")
    public String getTimbreTechnique() {
        return timbreTechnique;
    }

    @JsonProperty("timbreTechnique")
    public void setTimbreTechnique(String timbreTechnique) {
        this.timbreTechnique = timbreTechnique;
    }

    public Content withTimbreTechnique(String timbreTechnique) {
        this.timbreTechnique = timbreTechnique;
        return this;
    }

    @JsonProperty("timbre")
    public String getTimbre() {
        return timbre;
    }

    @JsonProperty("timbre")
    public void setTimbre(String timbre) {
        this.timbre = timbre;
    }

    public Content withTimbre(String timbre) {
        this.timbre = timbre;
        return this;
    }

    @JsonProperty("etablissement")
    public Object getEtablissement() {
        return etablissement;
    }

    @JsonProperty("etablissement")
    public void setEtablissement(Object etablissement) {
        this.etablissement = etablissement;
    }

    public Content withEtablissement(Object etablissement) {
        this.etablissement = etablissement;
        return this;
    }

    @JsonProperty("libelleCourt")
    public String getLibelleCourt() {
        return libelleCourt;
    }

    @JsonProperty("libelleCourt")
    public void setLibelleCourt(String libelleCourt) {
        this.libelleCourt = libelleCourt;
    }

    public Content withLibelleCourt(String libelleCourt) {
        this.libelleCourt = libelleCourt;
        return this;
    }

    @JsonProperty("libelleLong")
    public String getLibelleLong() {
        return libelleLong;
    }

    @JsonProperty("libelleLong")
    public void setLibelleLong(String libelleLong) {
        this.libelleLong = libelleLong;
    }

    public Content withLibelleLong(String libelleLong) {
        this.libelleLong = libelleLong;
        return this;
    }

    @JsonProperty("niveau")
    public String getNiveau() {
        return niveau;
    }

    @JsonProperty("niveau")
    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public Content withNiveau(String niveau) {
        this.niveau = niveau;
        return this;
    }

    @JsonProperty("uniteMere")
    public String getUniteMere() {
        return uniteMere;
    }

    @JsonProperty("uniteMere")
    public void setUniteMere(String uniteMere) {
        this.uniteMere = uniteMere;
    }

    public Content withUniteMere(String uniteMere) {
        this.uniteMere = uniteMere;
        return this;
    }

    @JsonProperty("agentResponsable")
    public String getAgentResponsable() {
        return agentResponsable;
    }

    @JsonProperty("agentResponsable")
    public void setAgentResponsable(String agentResponsable) {
        this.agentResponsable = agentResponsable;
    }

    public Content withAgentResponsable(String agentResponsable) {
        this.agentResponsable = agentResponsable;
        return this;
    }

    @JsonProperty("uniteEvaluation")
    public String getUniteEvaluation() {
        return uniteEvaluation;
    }

    @JsonProperty("uniteEvaluation")
    public void setUniteEvaluation(String uniteEvaluation) {
        this.uniteEvaluation = uniteEvaluation;
    }

    public Content withUniteEvaluation(String uniteEvaluation) {
        this.uniteEvaluation = uniteEvaluation;
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

    public Content withDateDebut(String dateDebut) {
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

    public Content withDateFin(Object dateFin) {
        this.dateFin = dateFin;
        return this;
    }

    @JsonProperty("dateEffetLibelle")
    public String getDateEffetLibelle() {
        return dateEffetLibelle;
    }

    @JsonProperty("dateEffetLibelle")
    public void setDateEffetLibelle(String dateEffetLibelle) {
        this.dateEffetLibelle = dateEffetLibelle;
    }

    public Content withDateEffetLibelle(String dateEffetLibelle) {
        this.dateEffetLibelle = dateEffetLibelle;
        return this;
    }

    @JsonProperty("region")
    public String getRegion() {
        return region;
    }

    @JsonProperty("region")
    public void setRegion(String region) {
        this.region = region;
    }

    public Content withRegion(String region) {
        this.region = region;
        return this;
    }

    @JsonProperty("uniteInsee")
    public Boolean getUniteInsee() {
        return uniteInsee;
    }

    @JsonProperty("uniteInsee")
    public void setUniteInsee(Boolean uniteInsee) {
        this.uniteInsee = uniteInsee;
    }

    public Content withUniteInsee(Boolean uniteInsee) {
        this.uniteInsee = uniteInsee;
        return this;
    }

    @JsonProperty("estAdministrative")
    public Boolean getEstAdministrative() {
        return estAdministrative;
    }

    @JsonProperty("estAdministrative")
    public void setEstAdministrative(Boolean estAdministrative) {
        this.estAdministrative = estAdministrative;
    }

    public Content withEstAdministrative(Boolean estAdministrative) {
        this.estAdministrative = estAdministrative;
        return this;
    }

    @JsonProperty("ssm")
    public Object getSsm() {
        return ssm;
    }

    @JsonProperty("ssm")
    public void setSsm(Object ssm) {
        this.ssm = ssm;
    }

    public Content withSsm(Object ssm) {
        this.ssm = ssm;
        return this;
    }

    @JsonProperty("frhl")
    public String getFrhl() {
        return frhl;
    }

    @JsonProperty("frhl")
    public void setFrhl(String frhl) {
        this.frhl = frhl;
    }

    public Content withFrhl(String frhl) {
        this.frhl = frhl;
        return this;
    }

    @JsonProperty("adresseFrance")
    public AdresseFrance getAdresseFrance() {
        return adresseFrance;
    }

    @JsonProperty("adresseFrance")
    public void setAdresseFrance(AdresseFrance adresseFrance) {
        this.adresseFrance = adresseFrance;
    }

    public Content withAdresseFrance(AdresseFrance adresseFrance) {
        this.adresseFrance = adresseFrance;
        return this;
    }

    @JsonProperty("adresseEtranger")
    public Object getAdresseEtranger() {
        return adresseEtranger;
    }

    @JsonProperty("adresseEtranger")
    public void setAdresseEtranger(Object adresseEtranger) {
        this.adresseEtranger = adresseEtranger;
    }

    public Content withAdresseEtranger(Object adresseEtranger) {
        this.adresseEtranger = adresseEtranger;
        return this;
    }

    @JsonProperty("estActive")
    public Boolean getEstActive() {
        return estActive;
    }

    @JsonProperty("estActive")
    public void setEstActive(Boolean estActive) {
        this.estActive = estActive;
    }

    public Content withEstActive(Boolean estActive) {
        this.estActive = estActive;
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

    public Content withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
