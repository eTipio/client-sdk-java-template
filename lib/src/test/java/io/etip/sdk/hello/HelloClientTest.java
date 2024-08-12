
package io.etip.sdk.hello;

import io.etip.sdk.hello.greetings.GetGreetingRequest;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HelloClientTest {


    @Test
    void createClientWithDefaultHttpClientAndCodecs() {
        HelloClient client = HelloClient.newBuilder()
                .secretKey("my-secret-key")
                .baseUri("http://localhost:8080")
                .build();
        assertNotNull(client);
        assertEquals("http://localhost:8080", client.baseUri());
    }

    @Test
    void createClientWithCustomHttpClientAndCodecs() {
        var httpClient = new OkHttpClient.Builder()
                .authenticator((route, response) -> {
                    return response.request().newBuilder().header("Authorization", "my-secret-key").build();
                })
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        JsonCodec jsonCodec = JsonCodec.newBuilder()
//                .decoder()
//                .encoder()
                .build();
        HelloClient client = HelloClient.newBuilder()
                .httpClient(httpClient)
                .codecs(jsonCodec)
                .baseUri("http://localhost:8080")
                .build();
        assertNotNull(client);
        assertEquals("http://localhost:8080", client.baseUri());
    }

    // An example of calling GreetingApis
    // @Test
    void callGetGreetingApis() {
        HelloClient client = HelloClient.newBuilder()
                .secretKey("my-secret-key")
                .build();
        var response = client.greetings().getGreeting(new GetGreetingRequest("Hantsy"));
        assertNotNull(response);
    }
}
