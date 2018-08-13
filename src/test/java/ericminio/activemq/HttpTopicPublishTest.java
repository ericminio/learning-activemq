package ericminio.activemq;

import ericminio.support.AsyncHttpResponse;
import ericminio.support.ThirdParty;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.web.MessageServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static ericminio.support.AsyncGetRequest.asyncGet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HttpTopicPublishTest {

    protected BrokerService broker;
    private Server server;
    private int port = 8080;
    String url = "http://localhost:" + port + "/message/this-topic";

    @Before
    public void startActiveMqBehindJetty() throws Exception {
        broker = new BrokerService();
        broker.setBrokerName("amq-broker");
        broker.start();
        broker.waitUntilStarted();

        ServletHolder holder = new ServletHolder(new MessageServlet());
        holder.setInitParameter("maximumReadTimeout", String.valueOf(Long.MAX_VALUE));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setInitParameter("org.apache.activemq.brokerURL", "vm://amq-broker");
        context.addServlet(holder, "/message/*");
        server = new Server(port);
        server.setHandler(context);
        server.start();
    }

    @After
    public void stopAll() throws Exception {
        server.stop();
        broker.stop();
        broker.waitUntilStopped();
    }

    @Test
    public void isDoneViaPost() throws Exception {
        AsyncHttpResponse client = asyncGet(url);
        ThirdParty.post(url, "body=hello");

        assertThat(client.getBody(), equalTo("hello"));
    }

    @Test
    public void canBeReadByTwoClients() throws Exception {
        AsyncHttpResponse client1 = asyncGet(url);
        AsyncHttpResponse client2 = asyncGet(url);
        ThirdParty.post(url, "body=hello");

        assertThat(client1.getBody(), equalTo("hello"));
        assertThat(client2.getBody(), equalTo("hello"));
    }

    @Test
    public void clientCanReadSeveralMessages() throws Exception {
        AsyncHttpResponse client = asyncGet(url);
        ThirdParty.post(url, "body=first");
        assertThat(client.getBody(), equalTo("first"));

        client = asyncGet(url);
        ThirdParty.post(url, "body=second");
        assertThat(client.getBody(), equalTo("second"));
    }
}