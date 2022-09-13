package fr.insee.rmes.external_services.notifications;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

@Service
public class RmesNotificationsImpl implements NotificationsContract {
	
	@Autowired
	Config config;
	
	private String brokerUrl;
	
	@PostConstruct
	public void init() {
		brokerUrl = "failover:(" + config.getBrokerUrl() + ")?randomize=false";
	}
	
    private static final Boolean NON_TRANSACTED = false;
    private static final long DELAY = 1;
    private static final long DUREE_VIE_MESSAGE = 300000;

    static final Logger logger = LogManager.getLogger(RmesNotificationsImpl.class);
	
	@Override
	public void notifyConceptCreation(String id, String uri) throws RmesException {
		logger.info("Notification : concept creation, id : {}", id);
		sendMessageToBrocker(RmesNotificationsMessages.conceptCreation(id, uri));
	}
	
	@Override
	public void notifyConceptUpdate(String id, String uri) throws RmesException {
		logger.info("Notification : concept update, id : {}", id);
		sendMessageToBrocker(RmesNotificationsMessages.conceptUpdate(id, uri));
	}
	
	@Override
	public void notifyCollectionCreation(String id, String uri) throws RmesException {
		logger.info("Notification : collection creation, id : {}", id);
		sendMessageToBrocker(RmesNotificationsMessages.collectionCreation(id, uri));
	}
	
	@Override
	public void notifyCollectionUpdate(String id, String uri) throws RmesException {
		logger.info("Notification : collection update, id : {}", id);
		sendMessageToBrocker(RmesNotificationsMessages.collectionUpdate(id, uri));
	}    
	
	public void sendMessageToBrocker(String message) throws RmesException {
        String url = brokerUrl;
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(config.getBrokerUser(), config.getBrokerPassword(), url);
        connectionFactory.setTrustedPackages(Arrays.asList("fr.insee.rmes"));
        Connection connection = null;

        try {

            connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic("topic-rmes");
            MessageProducer producer = session.createProducer(destination);
            producer.setTimeToLive(DUREE_VIE_MESSAGE);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            TextMessage msg = session.createTextMessage(message);
            producer.send(msg);
            Thread.sleep(DELAY);

            producer.close();
            session.close();

        } catch (Exception e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Fail to send notification");

        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
        			logger.error("JMS : Could not close an open connection...");
                }
            }
        }
    }
	
}
