package ericminio.activemq;

import ericminio.support.AsyncHttpResponse;
import ericminio.support.ThirdParty;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.web.MessageServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static ericminio.support.AsyncGetRequest.asyncGet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HttpPublishTest {

    protected BrokerService broker;
    private Server server;

    @Before
    public void startActiveMqBehindJetty() throws Exception {
        broker = new BrokerService();
        broker.setBrokerName("amq-broker");
        broker.setPersistent(false);
        broker.setUseJmx(true);
        broker.start();
        broker.waitUntilStarted();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setInitParameter("org.apache.activemq.brokerURL", "vm://amq-broker");
        context.addServlet(MessageServlet.class, "/message/*");
        server = new Server(8888);
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
        AsyncHttpResponse client = asyncGet("http://localhost:8888/message/this-queue?type=queue");
        ThirdParty.post("http://localhost:8888/message/this-queue?type=queue", "body=hello");

        assertThat(client.getBody(), equalTo("hello"));
    }
}