//
// Ce fichier a �t� g�n�r� par l'impl�mentation de r�f�rence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apport�e � ce fichier sera perdue lors de la recompilation du sch�ma source. 
// G�n�r� le : 2018.04.04 � 05:17:27 PM CEST 
//


package fr.insee.rmes.external_services.mail_sender;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fr.insee.xml.schema.outils package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fr.insee.xml.schema.outils
     * 
     */
    public ObjectFactory() {
    	//Create a new ObjectFactory 
    }

    /**
     * Create an instance of {@link SendRequest }
     * 
     */
    public SendRequest createSendRequest() {
        return new SendRequest();
    }

    /**
     * Create an instance of {@link MessageTemplate }
     * 
     */
    public MessageTemplate createMessageTemplate() {
        return new MessageTemplate();
    }

    /**
     * Create an instance of {@link NameValuePairType }
     * 
     */
    public NameValuePairType createNameValuePairType() {
        return new NameValuePairType();
    }

    /**
     * Create an instance of {@link ServiceConfiguration }
     * 
     */
    public ServiceConfiguration createServiceConfiguration() {
        return new ServiceConfiguration();
    }

    /**
     * Create an instance of {@link SendRequest.Recipients }
     * 
     */
    public SendRequest.Recipients createSendRequestRecipients() {
        return new SendRequest.Recipients();
    }

    /**
     * Create an instance of {@link MailingReport }
     * 
     */
    public MailingReport createMailingReport() {
        return new MailingReport();
    }

    /**
     * Create an instance of {@link ReportItem }
     * 
     */
    public ReportItem createReportItem() {
        return new ReportItem();
    }

    /**
     * Create an instance of {@link Recipient }
     * 
     */
    public Recipient createRecipient() {
        return new Recipient();
    }

}
