package ericminio.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ActiveMqTest {

    private ActiveMQConnectionFactory connectionFactory;
    private Connection clientConnection;
    private BrokerService broker;
    private String bindAddress = "http://localhost:61616";
    private String brokerUrl =bindAddress;

    @Before
    public void executeInOrder() throws Exception {
        startQueue();
        sendMessage();
    }
    @After
    public void stop() throws Exception {
        clientConnection.close();
        broker.stop();
    }

    public void startQueue() throws Exception {
        broker = new BrokerService();
        broker.addConnector(bindAddress);
        broker.start();
    }
    public void sendMessage() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue("any-subject");
        MessageProducer producer = session.createProducer(destination);
        TextMessage message = session.createTextMessage("Hello");
        producer.send(message);
        connection.close();
    }

    @Test
    public void canBroadcastTextMessage() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        clientConnection = connectionFactory.createConnection();
        clientConnection.start();
        Session session = clientConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue("any-subject");
        MessageConsumer consumer = session.createConsumer(destination);
        TextMessage message = (TextMessage) consumer.receive();

        assertThat(message.getText(), equalTo("Hello"));
    }
}
