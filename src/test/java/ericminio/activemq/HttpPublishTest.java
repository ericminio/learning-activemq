package ericminio.activemq;

import ericminio.support.AsyncHttpResponse;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.web.MessageServlet;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import static ericminio.support.AsyncGetRequest.asyncGet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HttpPublishTest {

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

        server = new Server(8888);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setInitParameter("org.apache.activemq.brokerURL", "vm://amq-broker");
        context.setContextPath("");
        context.addServlet(new ServletHolder(new MessageServlet()), "/message/*");
        server.setHandler(context);
        server.start();
    }

    @After
    public void stopAll() throws Exception {
        session.close();
        connection.close();
        if (server != null) { server.stop(); }
        broker.stop();
        broker.waitUntilStopped();
    }

    @Test
    public void isDoneViaPost() throws Exception {
        AsyncHttpResponse response = asyncGet("http://localhost:8888/message/this-queue?type=queue");
        String content = "body=hello";
        HttpClient httpClient = new HttpClient();
        httpClient.start();
        httpClient
            .newRequest("http://localhost:8888/message/this-queue?type=queue")
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .content(new StringContentProvider(content))
            .method(HttpMethod.POST)
            .send();

        assertThat(response.getBody(), equalTo("hello"));
    }
}