//
// Ce fichier a �t� g�n�r� par l'impl�mentation de r�f�rence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apport�e � ce fichier sera perdue lors de la recompilation du sch�ma source. 
// G�n�r� le : 2018.04.04 � 05:17:27 PM CEST 
//


package fr.insee.rmes.persistance.mail_sender.rmes;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;element ref="{http://xml.insee.fr/schema/outils}ServiceConfiguration" minOccurs="0"/>
 *         &lt;element ref="{http://xml.insee.fr/schema/outils}MessageTemplate"/>
 *         &lt;element name="Recipients">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element ref="{http://xml.insee.fr/schema/outils}Recipient" maxOccurs="unbounded"/>
 *                   &lt;element name="RecipientListReference" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "serviceConfiguration",
    "messageTemplate",
    "recipients"
})
@XmlRootElement(name = "SendRequest")
public class SendRequest {

    @XmlElement(name = "ServiceConfiguration")
    protected ServiceConfiguration serviceConfiguration;
    @XmlElement(name = "MessageTemplate", required = true)
    protected MessageTemplate messageTemplate;
    @XmlElement(name = "Recipients", required = true)
    protected SendRequest.Recipients recipients;

    /**
     * Obtient la valeur de la propri�t� serviceConfiguration.
     * 
     * @return
     *     possible object is
     *     {@link ServiceConfiguration }
     *     
     */
    public ServiceConfiguration getServiceConfiguration() {
        return serviceConfiguration;
    }

    /**
     * D�finit la valeur de la propri�t� serviceConfiguration.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceConfiguration }
     *     
     */
    public void setServiceConfiguration(ServiceConfiguration value) {
        this.serviceConfiguration = value;
    }

    /**
     * Obtient la valeur de la propri�t� messageTemplate.
     * 
     * @return
     *     possible object is
     *     {@link MessageTemplate }
     *     
     */
    public MessageTemplate getMessageTemplate() {
        return messageTemplate;
    }

    /**
     * D�finit la valeur de la propri�t� messageTemplate.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageTemplate }
     *     
     */
    public void setMessageTemplate(MessageTemplate value) {
        this.messageTemplate = value;
    }

    /**
     * Obtient la valeur de la propri�t� recipients.
     * 
     * @return
     *     possible object is
     *     {@link SendRequest.Recipients }
     *     
     */
    public SendRequest.Recipients getRecipients() {
        return recipients;
    }

    /**
     * D�finit la valeur de la propri�t� recipients.
     * 
     * @param value
     *     allowed object is
     *     {@link SendRequest.Recipients }
     *     
     */
    public void setRecipients(SendRequest.Recipients value) {
        this.recipients = value;
    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de sch�ma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice>
     *         &lt;element ref="{http://xml.insee.fr/schema/outils}Recipient" maxOccurs="unbounded"/>
     *         &lt;element name="RecipientListReference" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "recipient",
        "recipientListReference"
    })
    public static class Recipients {

        @XmlElement(name = "Recipient")
        protected List<Recipient> recipient;
        @XmlElement(name = "RecipientListReference")
        @XmlSchemaType(name = "anyURI")
        protected String recipientListReference;

        /**
         * Gets the value of the recipient property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the recipient property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRecipient().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Recipient }
         * 
         * 
         */
        public List<Recipient> getRecipient() {
            if (recipient == null) {
                recipient = new ArrayList<>();
            }
            return this.recipient;
        }

        /**
         * Obtient la valeur de la propri�t� recipientListReference.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRecipientListReference() {
            return recipientListReference;
        }

        /**
         * D�finit la valeur de la propri�t� recipientListReference.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRecipientListReference(String value) {
            this.recipientListReference = value;
        }

    }

}
