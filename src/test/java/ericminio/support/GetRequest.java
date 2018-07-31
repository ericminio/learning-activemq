package ericminio.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetRequest {

    public static HttpResponse get(String url) throws Exception {
        HttpURLConnection request = (HttpURLConnection) new URL( url ).openConnection();
        HttpResponse response = new HttpResponse();
        response.setStatusCode(request.getResponseCode());
        if (request.getResponseCode() < 400) {
            response.setBody(getResponseBody(request.getInputStream()));
        } else {
            response.setBody(getResponseBody(request.getErrorStream()));
        }
        request.disconnect();

        return response;
    }

    protected static String getResponseBody(InputStream inputStream) throws IOException {
        byte[] response = new byte[ inputStream.available() ];
        inputStream.read( response );
        return new String(response);
    }
}
