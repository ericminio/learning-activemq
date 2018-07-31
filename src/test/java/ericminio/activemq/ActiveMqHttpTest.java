package ericminio.activemq;

import ericminio.support.AsyncHttpResponse;
import ericminio.support.JettyTestSupport;
import org.junit.Test;

import static ericminio.support.AsyncGetRequest.asyncGet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ActiveMqHttpTest extends JettyTestSupport {

    @Test
    public void queueCanBeReadViaHttpGet() throws Exception {
        AsyncHttpResponse response = asyncGet("http://localhost:" + getPort() + "/message/test?readTimeout=1000&type=queue");
        producer.send(session.createTextMessage("hello"));

        assertThat(response.getBody(), equalTo("hello"));
    }
}