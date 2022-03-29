package fr.insee.rmes.external_services.mail_sender;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsExportBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.external_services.mail_sender.SendRequest.Recipients;
import fr.insee.rmes.model.mail_sender.Mail;
import fr.insee.rmes.model.mail_sender.MailSenderContract;
import fr.insee.rmes.utils.FilesUtils;

@Service
public class RmesMailSenderImpl implements MailSenderContract {
	
	@Autowired
	Config config;
	
	@Autowired
	ConceptsExportBuilder conceptsExport;
	
	@Autowired
	StampsRestrictionsService stampsRestrictionsService;
	

	static final Logger logger = LogManager.getLogger(RmesMailSenderImpl.class);
		
	@Override
	public boolean sendMailConcept(String id, String body, Map<String,InputStream> getFileToJoin) throws  RmesException  {
		IRI conceptURI = RdfUtils.conceptIRI(id);
		if (!stampsRestrictionsService.isConceptOrCollectionOwner(conceptURI)) {
			throw new RmesUnauthorizedException(ErrorCodes.CONCEPT_MAILING_RIGHTS_DENIED,"mailing rights denied",id);
		}
		Mail mail = prepareMail(body);
		String filename = getFileToJoin.entrySet().iterator().next().getKey();
		try(InputStream is = getFileToJoin.get(filename)){
			return sendMail(mail, is, filename );
		} catch (IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
		}
	}
	
	@Override
	public boolean sendMailCollection(String id, String body, Map<String,InputStream> getFileToJoin) throws  RmesException  {
		IRI collectionURI = RdfUtils.collectionIRI(id);
		if (!stampsRestrictionsService.isConceptOrCollectionOwner(collectionURI)) {
			throw new RmesUnauthorizedException(ErrorCodes.COLLECTION_MAILING_RIGHTS_DENIED,"mailing rights denied",id);
		}
		Mail mail = prepareMail(body);
		String filename = getFileToJoin.entrySet().iterator().next().getKey();
		try(InputStream is = getFileToJoin.get(filename)){
			return sendMail(mail, is, filename);
		}catch (IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
		}
	}
		
	private boolean sendMail(Mail mail, InputStream is, String fileName) {
		fileName = FilesUtils.cleanFileNameAndAddExtension(fileName,"odt");
		String encodedFileName = fileName ;
				//new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
		/*try {
			encodedFileName = URLEncoder.encode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
		}*/
		
		
		MessageTemplate messagetemplate = new MessageTemplate();

		NameValuePairType nameValuePairType = new NameValuePairType();
		nameValuePairType.setName("Content-Type");
		nameValuePairType.setValue("text/html; charset=UTF-8");
		messagetemplate.getHeader().add(nameValuePairType);
		messagetemplate.setSender(mail.getSender());
		messagetemplate.setSubject(mail.getObject());
		messagetemplate.setContent(mail.getMessage());

		// PJ
		List<String> attachments = new ArrayList<>();
		attachments.add(fileName);
		
		// création des destinataires
		Recipient destinataire1 = new Recipient();
		destinataire1.setAddress(mail.getRecipient());
		destinataire1.getAttachments().add(encodedFileName);
		Recipients destinataires = new Recipients();
		destinataires.getRecipient().add(destinataire1);

		// préparation de la requête à envoyer
		SendRequest request = new SendRequest();
		request.setMessageTemplate(messagetemplate);
		request.setRecipients(destinataires);
		
		// Contenu html
		NameValuePairType nameValuePairTypeSmtpFrom = new NameValuePairType();
		nameValuePairTypeSmtpFrom.setName("mail.smtp.from");
		nameValuePairTypeSmtpFrom.setValue(mail.getSender());
		ServiceConfiguration configService = new ServiceConfiguration();
		configService.getSMTPProperties().add(nameValuePairTypeSmtpFrom);
		request.setServiceConfiguration(configService);

		// création d'un client authentifié pour SPOC	
		HttpAuthenticationFeature authentificationFeature = HttpAuthenticationFeature
				.basic(config.getSpocUser(), config.getSpocPassword());
		Client client = ClientBuilder.newClient()
				.register(authentificationFeature);
		
		// Multipart
		client.register(MultiPartFeature.class);
		
		MultiPart mp = new FormDataMultiPart();
		mp.bodyPart(new FormDataBodyPart(FormDataContentDisposition.name("request").build(),request,MediaType.APPLICATION_XML_TYPE));

		final StreamDataBodyPart bodyPart = new StreamDataBodyPart("attachments", is, encodedFileName);
		mp.bodyPart(bodyPart);
		


		Variant variant = new Variant(MediaType.MULTIPART_FORM_DATA_TYPE.withCharset("UTF-8"), config.getLg1(), "utf-8");
		Entity<MultiPart> entity = Entity.entity(mp, variant);
		String result = client
							.target(config.getSpocServiceUrl())
							.request()
							.post(entity,String.class);
		return isMailSent(result);
	}


	
	private Mail prepareMail(String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		Mail mail = new Mail();
		try {
			mail = mapper.readValue(body, Mail.class);
		} catch (IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
		}
		return mail;
	}
	
	private boolean isMailSent(String result) {
		JSONObject response = new JSONObject(result);
		if (response.get("ReportItem").toString() != null) {
			JSONArray reportItem = (JSONArray) response.get("ReportItem");
			JSONObject firstReportItem = (JSONObject) reportItem.get(0);
			return firstReportItem.getBoolean("sent");
		} else {
			return false;
		}
	}
}
