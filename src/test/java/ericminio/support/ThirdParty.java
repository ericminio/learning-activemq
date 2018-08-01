package ericminio.support;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;

public class ThirdParty {

    public static void post(String uri, String content) throws Exception {
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
