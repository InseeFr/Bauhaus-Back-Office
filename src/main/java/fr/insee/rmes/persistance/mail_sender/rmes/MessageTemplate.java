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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;element name="Header" type="{http://xml.insee.fr/schema/outils}NameValuePairType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Sender" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="Subject" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *         &lt;choice>
 *           &lt;element name="Content" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="ContentReference" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;/choice>
 *         &lt;element name="PlainTextContent" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "header",
    "sender",
    "subject",
    "content",
    "contentReference",
    "plainTextContent"
})
@XmlRootElement(name = "MessageTemplate")
public class MessageTemplate {

    @XmlElement(name = "Header")
    protected List<NameValuePairType> header;
    @XmlElement(name = "Sender", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String sender;
    @XmlElement(name = "Subject", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String subject;
    @XmlElement(name = "Content")
    protected String content;
    @XmlElement(name = "ContentReference")
    @XmlSchemaType(name = "anyURI")
    protected String contentReference;
    @XmlElement(name = "PlainTextContent", required = true)
    protected String plainTextContent;

    /**
     * Gets the value of the header property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the header property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHeader().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NameValuePairType }
     * 
     * 
     */
    public List<NameValuePairType> getHeader() {
        if (header == null) {
            header = new ArrayList<>();
        }
        return this.header;
    }

    /**
     * Get the value of sender property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSender() {
        return sender;
    }

    /**
     * D�finit la valeur de la propri�t� sender.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSender(String value) {
        this.sender = value;
    }

    /**
     * Obtient la valeur de la propri�t� subject.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubject() {
        return subject;
    }

    /**
     * D�finit la valeur de la propri�t� subject.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubject(String value) {
        this.subject = value;
    }

    /**
     * Obtient la valeur de la propri�t� content.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContent() {
        return content;
    }

    /**
     * D�finit la valeur de la propri�t� content.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContent(String value) {
        this.content = value;
    }

    /**
     * Obtient la valeur de la propri�t� contentReference.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentReference() {
        return contentReference;
    }

    /**
     * D�finit la valeur de la propri�t� contentReference.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentReference(String value) {
        this.contentReference = value;
    }

    /**
     * Obtient la valeur de la propri�t� plainTextContent.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlainTextContent() {
        return plainTextContent;
    }

    /**
     * D�finit la valeur de la propri�t� plainTextContent.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlainTextContent(String value) {
        this.plainTextContent = value;
    }

}
