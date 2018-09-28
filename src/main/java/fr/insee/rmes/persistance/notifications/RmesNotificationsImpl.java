package fr.insee.rmes.persistance.notifications;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class RmesNotificationsImpl implements NotificationsContract {
	
	private static final String BROKER_URL = "failover:(" + Config.BROKER_URL + ")?randomize=false";
	
    private static final Boolean NON_TRANSACTED = false;
    private static final long DELAY = 1;
    private static final long DUREE_VIE_MESSAGE = 300000;

    static final Logger logger = LogManager.getLogger(RmesNotificationsImpl.class);
	
	public void notifyConceptCreation(String id, String uri) throws RmesException {
		logger.info("Notification : concept creation, id : " + id);
		sendMessageToBrocker(RmesNotificationsMessages.conceptCreation(id, uri));
	}
	
	public void notifyConceptUpdate(String id, String uri) throws RmesException {
		logger.info("Notification : concept update, id : " + id);
		sendMessageToBrocker(RmesNotificationsMessages.conceptUpdate(id, uri));
	}
	
	public void notifyCollectionCreation(String id, String uri) throws RmesException {
		logger.info("Notification : collection creation, id : " + id);
		sendMessageToBrocker(RmesNotificationsMessages.collectionCreation(id, uri));
	}
	
	public void notifyCollectionUpdate(String id, String uri) throws RmesException {
		logger.info("Notification : collection update, id : " + id);
		sendMessageToBrocker(RmesNotificationsMessages.collectionUpdate(id, uri));
	}    
	
	public void sendMessageToBrocker(String message) throws RmesException {
        String url = BROKER_URL;
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Config.BROKER_USER, Config.BROKER_PASSWORD, url);
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


            // tell the subscribers we're done
            //producer.send(session.createTextMessage("END"));

            producer.close();
            session.close();

        } catch (Exception e) {
			throw new RmesException(500, e.getMessage(), "Fail to send notification");

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
