//
// Ce fichier a �t� g�n�r� par l'impl�mentation de r�f�rence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apport�e � ce fichier sera perdue lors de la recompilation du sch�ma source. 
// G�n�r� le : 2018.04.04 � 05:17:27 PM CEST 
//


package fr.insee.rmes.external_services.mail_sender;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element name="GeneralMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{http://xml.insee.fr/schema/outils}ReportItem" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="total" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="sent" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "generalMessage",
    "reportItem"
})
@XmlRootElement(name = "MailingReport")
public class MailingReport {

    @XmlElement(name = "GeneralMessage", required = true)
    protected String generalMessage;
    @XmlElement(name = "ReportItem", required = true)
    protected List<ReportItem> reportItem;
    @XmlAttribute(name = "total", required = true)
    protected int total;
    @XmlAttribute(name = "sent", required = true)
    protected int sent;

    /**
     * Obtient la valeur de la propri�t� generalMessage.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeneralMessage() {
        return generalMessage;
    }

    /**
     * D�finit la valeur de la propri�t� generalMessage.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeneralMessage(String value) {
        this.generalMessage = value;
    }

    /**
     * Gets the value of the reportItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reportItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReportItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReportItem }
     * 
     * 
     */
    public List<ReportItem> getReportItem() {
        if (reportItem == null) {
            reportItem = new ArrayList<>();
        }
        return this.reportItem;
    }

    /**
     * Obtient la valeur de la propri�t� total.
     * 
     */
    public int getTotal() {
        return total;
    }

    /**
     * D�finit la valeur de la propri�t� total.
     * 
     */
    public void setTotal(int value) {
        this.total = value;
    }

    /**
     * Obtient la valeur de la propri�t� sent.
     * 
     */
    public int getSent() {
        return sent;
    }

    /**
     * D�finit la valeur de la propri�t� sent.
     * 
     */
    public void setSent(int value) {
        this.sent = value;
    }

}
