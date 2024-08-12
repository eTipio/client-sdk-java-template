package io.etip.sdk.hello;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.etip.sdk.hello.greetings.GreetingsApi;
import io.etip.sdk.hello.greetings.impl.GreetingsApiImpl;
import okhttp3.OkHttpClient;

public class HelloClient {

    private OkHttpClient httpClient;
    private JsonCodec codecs;
    private String baseUri;

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public JsonCodec getCodecs() {
        return codecs;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public GreetingsApi greetings() {
        return new GreetingsApiImpl(this);
    }

    // builder pattern to setup the client.
    public static Builder newBuilder() {
        return new Builder();
    }

    static class Builder {

        private String secretKey;
        private OkHttpClient httpClient;
        private JsonCodec codecs;
        private String baseUri;


        public Builder httpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder codecs(JsonCodec codec) {
            this.codecs = codec;
            return this;
        }

        public Builder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public HelloClient build() {
            var client = new HelloClient();

            if (this.httpClient == null) {
                OkHttpClient.Builder httpClientBuilder = new OkHttpClient().newBuilder();

                if (this.secretKey != null) {
                    httpClientBuilder = httpClientBuilder
                            .authenticator((route, response) -> {
                                return response.request().newBuilder().header("Authorization", this.secretKey).build();
                            });
                }

                this.httpClient = httpClientBuilder.build();
            }

            if (this.codecs == null) {
                var objectMapper = new ObjectMapper();
                this.codecs = JsonCodec.newBuilder()
//                        .decoder(new ObjectMapperDecoder(objectMapper))
//                        .encoder(new ObjectMapperEncoder(objectMapper))
                        .build();
            }


            client.baseUri = this.baseUri;
            client.httpClient = this.httpClient;
            client.codecs = this.codecs;
            return client;
        }
    }
}
