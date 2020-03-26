//
// Ce fichier a �t� g�n�r� par l'impl�mentation de r�f�rence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apport�e � ce fichier sera perdue lors de la recompilation du sch�ma source. 
// G�n�r� le : 2018.04.04 � 05:17:27 PM CEST 
//


package fr.insee.rmes.service.mail_sender;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de sch�ma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SMTPProperties" type="{http://xml.insee.fr/schema/outils}NameValuePairType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ServiceProperties" type="{http://xml.insee.fr/schema/outils}NameValuePairType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "smtpProperties",
    "serviceProperties"
})
@XmlRootElement(name = "ServiceConfiguration")
public class ServiceConfiguration {

    @XmlElement(name = "SMTPProperties")
    protected List<NameValuePairType> smtpProperties;
    @XmlElement(name = "ServiceProperties")
    protected List<NameValuePairType> serviceProperties;

    /**
     * Gets the value of the smtpProperties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the smtpProperties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSMTPProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NameValuePairType }
     * 
     * 
     */
    public List<NameValuePairType> getSMTPProperties() {
        if (smtpProperties == null) {
            smtpProperties = new ArrayList<>();
        }
        return this.smtpProperties;
    }

    /**
     * Gets the value of the serviceProperties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceProperties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NameValuePairType }
     * 
     * 
     */
    public List<NameValuePairType> getServiceProperties() {
        if (serviceProperties == null) {
            serviceProperties = new ArrayList<>();
        }
        return this.serviceProperties;
    }

}
