package ericminio.support;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;

public class PostRequest {

    public static void post(String uri, String text) throws Exception {
        String content = "body=" + text;
        HttpClient httpClient = new HttpClient();
        httpClient.start();
        httpClient
                .newRequest(uri)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .content(new StringContentProvider(content))
                .method(HttpMethod.POST)
                .send();
    }
}
