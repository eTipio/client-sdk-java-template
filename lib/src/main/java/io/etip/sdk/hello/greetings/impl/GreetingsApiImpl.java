package io.etip.sdk.hello.greetings.impl;

import io.etip.sdk.hello.HelloClient;
import io.etip.sdk.hello.greetings.GetGreetingRequest;
import io.etip.sdk.hello.greetings.GetGreetingResponse;
import io.etip.sdk.hello.greetings.GreetingFailedException;
import io.etip.sdk.hello.greetings.GreetingsApi;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class GreetingsApiImpl implements GreetingsApi {
    private final HelloClient client;


    public GreetingsApiImpl(HelloClient client) {
        this.client = client;
    }

    @Override
    public GetGreetingResponse getGreeting(GetGreetingRequest getGreetingRequest) {
        var requestUrl = this.client.baseUri() + "/greetings?name=" + getGreetingRequest.name();
        Response response = null;
        try {
            response = this.client.httpClient()
                    .newCall(new Request.Builder().get().url(requestUrl).build())
                    .execute();
        } catch (IOException e) {
            throw new GreetingFailedException(e.getMessage());
        }

        if (response.code() != 200) {
            throw new GreetingFailedException("Failed to get greeting: ");
        }

        try {
            return this.client.codecs().decoder().decode(response.body().string(), GetGreetingResponse.class);
        } catch (IOException e) {
            throw new GreetingFailedException(e.getMessage());
        }
    }
}
