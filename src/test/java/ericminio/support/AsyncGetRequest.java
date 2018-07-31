package ericminio.support;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class AsyncGetRequest {

    public static AsyncHttpResponse asyncGet(String url) throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();
        HttpClient httpClient = new HttpClient();
        httpClient.start();
        httpClient.newRequest(url).send(new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                future.complete(getContentAsString());
            }
        });
        AsyncHttpResponse response = new AsyncHttpResponse(future);

        return response;
    }

}
