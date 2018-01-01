package fr.insee.rmes.persistance.mailSender;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.archi.spoc.content.MessageTemplate;
import fr.insee.archi.spoc.content.NameValuePairType;
import fr.insee.archi.spoc.content.Recipient;
import fr.insee.archi.spoc.content.SendRequest;
import fr.insee.archi.spoc.content.SendRequest.Recipients;
import fr.insee.archi.spoc.content.ServiceConfiguration;
import fr.insee.archi.spoc.content.Validate;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.service.sesame.export.Export;
import fr.insee.rmes.persistance.service.sesame.export.Jasper;

public class RmesMailSenderImpl implements MailSenderContract {
		
	public boolean sendMailConcept(String id, String body) {
		Mail mail = prepareMail(body);
		JSONObject json = new Export().getConceptData(id);
		Jasper jasper = new Jasper();
		InputStream is = jasper.exportConcept(json, "default Mime");
		return sendMail(mail, is, json);
	}
	
	public boolean sendMailCollection(String id, String body) {
		Mail mail = prepareMail(body);
		JSONObject json = new Export().getCollectionData(id);
		Jasper jasper = new Jasper();
		InputStream is = jasper.exportCollection(json, "default Mime");
		return sendMail(mail, is, json);
	}
		
	private boolean sendMail(Mail mail, InputStream is, JSONObject json) {
		
		String fileName = json.getString("prefLabelLg1");
		fileName = Normalizer.normalize(fileName.toLowerCase()
				.replaceAll(" ", "-"), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "") + ".odt";
		
		MessageTemplate messagetemplate = new MessageTemplate();

		NameValuePairType nameValuePairType = new NameValuePairType();
		nameValuePairType.setName("Content-Type");
		nameValuePairType.setValue("text/html; charset=UTF-8");
		messagetemplate.getHeader().add(nameValuePairType);
		messagetemplate.setSender(mail.getSender());
		messagetemplate.setSubject(mail.getObject());
		messagetemplate.setContent(mail.getMessage());

		// PJ
		List<String> attachments = new ArrayList<String>();
		attachments.add(fileName);
		
		// création des destinataires
		Recipient destinataire1 = new Recipient();
		destinataire1.setAddress(mail.getRecipient());
		destinataire1.setAttachments(attachments);
		Recipients destinataires = new Recipients();
		destinataires.getRecipients().add(destinataire1);

		// préparation de la requête à envoyer
		SendRequest request = new SendRequest();
		request.setMessageTemplate(messagetemplate);
		request.setRecipients(destinataires);
		
		// Contenu html
		NameValuePairType nameValuePairTypeSmtpFrom = new NameValuePairType();
		nameValuePairTypeSmtpFrom.setName(Validate.SMTPFROM_PROPERTY);
		nameValuePairTypeSmtpFrom.setValue(mail.getSender());
		ServiceConfiguration config = new ServiceConfiguration();
		config.getSMTPProperties().add(nameValuePairTypeSmtpFrom);
		request.setServiceConfiguration(config);

		// création d'un client authentifié pour SPOC	
		HttpAuthenticationFeature authentificationFeature = HttpAuthenticationFeature
				.basic(Config.SPOC_USER, Config.SPOC_PASSWORD);
		Client client = ClientBuilder.newClient().register(
				authentificationFeature);
			
		// Multipart
		
		client.register(MultiPartFeature.class);
		
		FormDataMultiPart mp = new FormDataMultiPart();
		mp.bodyPart(new FormDataBodyPart(FormDataContentDisposition.name("request").build(),request,MediaType.APPLICATION_XML_TYPE));
		final StreamDataBodyPart bodyPart = new StreamDataBodyPart("attachments", is, fileName);
		mp.bodyPart(bodyPart);
				
		String result = client.target(Config.SPOC_SERVICE_URL)
				.request()
				.post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA_TYPE),String.class); 	
		return isMailSent(result);
	}
	
	private Mail prepareMail(String body) {
		ObjectMapper mapper = new ObjectMapper();
		Mail mail = new Mail();
		try {
			mail = mapper.readValue(body, Mail.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mail;
	}
	
	private boolean isMailSent(String result) {
		JSONObject response = new JSONObject(result);
		if (response.get("ReportItem").toString() != null) {
			JSONArray reportItem = (JSONArray) response.get("ReportItem");
			JSONObject firstReportItem = (JSONObject) reportItem.get(0);
			boolean sent = firstReportItem.getBoolean("sent");
			return sent;
		}
		else return false;
	}
}
