package ericminio.support;

import java.util.concurrent.CompletableFuture;

public class AsyncHttpResponse {

    private CompletableFuture<String> future;

    public AsyncHttpResponse(CompletableFuture<String> future) {
        this.future = future;
    }

    public String getBody() throws Exception {
        return future.get();
    }

}
