package ericminio.activemq;

import ericminio.support.HttpResponse;
import ericminio.support.JettyTestSupport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static ericminio.support.GetRequest.get;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

public class ActiveMqHttpTest extends JettyTestSupport {


    private static final Logger LOG = LoggerFactory.getLogger(ActiveMqHttpTest.class);

    @Test
    public void testConsume() throws Exception {
        int port = getPort();


        HttpClient httpClient = new HttpClient();
        httpClient.start();

        final StringBuffer buf = new StringBuffer();
        final CountDownLatch latch =
                asyncRequest(httpClient, "http://localhost:" + port + "/message/test?readTimeout=1000&type=queue", buf);

        producer.send(session.createTextMessage("test"));
        LOG.info("message sent");

        latch.await();
        assertEquals("test", buf.toString());
    }

    protected CountDownLatch asyncRequest(final HttpClient httpClient, final String url, final StringBuffer buffer) {
        final CountDownLatch latch = new CountDownLatch(1);
        httpClient.newRequest(url).send(new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                buffer.append(getContentAsString());
                latch.countDown();
            }
        });
        return latch;
    }

    @Test
    public void canBroadcastTextMessage() throws Exception {
        producer.send(session.createTextMessage("test"));
        int port = getPort();
        HttpResponse response = get( "http://localhost:" + port + "/message/test?readTimeout=1000&type=queue" );

        Assert.assertThat(response.getStatusCode(), equalTo(200 ));
        Assert.assertThat(response.getBody(), equalTo( "test" ));
    }
}
