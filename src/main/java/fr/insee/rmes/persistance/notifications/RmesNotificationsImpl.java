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

public class RmesNotificationsImpl implements NotificationsContract {
	
	private static String BROKER_URL = "failover:(" + Config.BROKER_URL + ")?randomize=false";
	
    private static final Boolean NON_TRANSACTED = false;
    private static final long DELAY = 1;
    private static final long DUREE_VIE_MESSAGE = 300000;

	final static Logger logger = LogManager.getLogger(RmesNotificationsImpl.class);
	
	public void notifyConceptCreation(String id, String URI) {
		logger.info("Notification : concept creation, id : " + id);
		sendMessageToBrocker(RmesNotificationsMessages.conceptCreation(id, URI));
	}
	
	public void notifyConceptUpdate(String id, String URI) {
		logger.info("Notification : concept update, id : " + id);
		sendMessageToBrocker(RmesNotificationsMessages.conceptUpdate(id, URI));
	}
	
	public void notifyCollectionCreation(String id, String URI) {
		logger.info("Notification : collection creation, id : " + id);
		sendMessageToBrocker(RmesNotificationsMessages.collectionCreation(id, URI));
	}
	
	public void notifyCollectionUpdate(String id, String URI) {
		logger.info("Notification : collection update, id : " + id);
		sendMessageToBrocker(RmesNotificationsMessages.collectionUpdate(id, URI));
	}    
	
	public void sendMessageToBrocker(String message) {
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
            System.out.println("JMS : " + e.getMessage());
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    System.out.println("JMS : Could not close an open connection...");
                }
            }
        }
    }
	
}
