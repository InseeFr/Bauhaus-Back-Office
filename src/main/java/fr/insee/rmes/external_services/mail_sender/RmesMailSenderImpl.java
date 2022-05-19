package fr.insee.rmes.external_services.mail_sender;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
import fr.insee.rmes.utils.RestTemplateUtils;

@Service
public class RmesMailSenderImpl implements MailSenderContract {
	
	@Autowired
	Config config;
	
	@Autowired
	ConceptsExportBuilder conceptsExport;
	
	@Autowired
	StampsRestrictionsService stampsRestrictionsService;
	
	@Autowired
	RestTemplateUtils restTemplateUtils;
	

	static final Logger logger = LogManager.getLogger(RmesMailSenderImpl.class);
		
	@Override
	public boolean sendMailConcept(String id, String body, Map<String,InputStream> getFileToJoin) throws  RmesException  {
		IRI conceptURI = RdfUtils.conceptIRI(id);
		if (!stampsRestrictionsService.isConceptOrCollectionOwner(conceptURI)) {
			throw new RmesUnauthorizedException(ErrorCodes.CONCEPT_MAILING_RIGHTS_DENIED,"mailing rights denied",id);
		}
		return sendMail(body, getFileToJoin);
	}

	public boolean sendMail(String body, Map<String, InputStream> getFileToJoin) throws RmesException {
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
		return sendMail(body, getFileToJoin);
	}
		
	private boolean sendMail(Mail mail, InputStream is, String fileName) {
		fileName = FilesUtils.cleanFileNameAndAddExtension(fileName,"odt");
		String encodedFileName = fileName ;	
		
		MessageTemplate messagetemplate = new MessageTemplate();

		NameValuePairType nameValuePairType = new NameValuePairType();
		nameValuePairType.setName("Content-Type");
		nameValuePairType.setValue("text/html; charset=UTF-8");
		messagetemplate.getHeader().add(nameValuePairType);
		messagetemplate.setSender(mail.getSender());
		messagetemplate.setSubject(mail.getObject());
		messagetemplate.setContent(mail.getMessage());
		
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
		//Rest Client to get all
		HttpHeaders headers = restTemplateUtils.getHeadersWithBasicAuth(config.getSpocUser(), config.getSpocPassword());
		restTemplateUtils.addMultipartContentToHeader(headers);

		//Get file as a resource
		Resource fileResource = new InputStreamResource(is);
		
		//Call mail sender services
		String result = restTemplateUtils.postForEntity(config.getSpocServiceUrl(),request, headers, fileResource);

		
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
