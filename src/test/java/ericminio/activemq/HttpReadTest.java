package ericminio.activemq;

import ericminio.support.AsyncHttpResponse;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.web.MessageServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.net.URI;

import static ericminio.support.AsyncGetRequest.asyncGet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HttpReadTest {

    protected BrokerService broker;
    protected Session session;
    protected MessageProducer producer;
    protected URI tcpUri;
    protected URI stompUri;

    private Server server;
    private ActiveMQConnectionFactory factory;
    private Connection connection;

    @Before
    public void startActiveMq() throws Exception {
        broker = new BrokerService();
        broker.setBrokerName("amq-broker");
        broker.setPersistent(false);
        broker.setDataDirectory("target/activemq-data");
        broker.setUseJmx(true);
        tcpUri = new URI(broker.addConnector("tcp://localhost:0").getPublishableConnectString());
        stompUri = new URI(broker.addConnector("stomp://localhost:0").getPublishableConnectString());
        broker.start();
        broker.waitUntilStarted();
        factory = new ActiveMQConnectionFactory(tcpUri);
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producer = session.createProducer(session.createQueue("this-queue"));

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setInitParameter("org.apache.activemq.brokerURL", "vm://amq-broker");
        context.addServlet(MessageServlet.class, "/message/*");
        server = new Server(8888);
        server.setHandler(context);
        server.start();
    }

    @After
    public void stopAll() throws Exception {
        session.close();
        connection.close();
        server.stop();
        broker.stop();
        broker.waitUntilStopped();
    }

    @Test
    public void isDoneViaGet() throws Exception {
        AsyncHttpResponse response = asyncGet("http://localhost:8888/message/this-queue?type=queue");
        producer.send(session.createTextMessage("hello"));

        assertThat(response.getBody(), equalTo("hello"));
    }
}