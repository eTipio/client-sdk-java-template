# Java Client SDK Template for HTTP APIs

In the previous development, we faced the problem that a lot of third-party APIs did not provide a Java or Kotlin specific client SDK for developers to simplify the API call. We (the API end users) have to use our familiar technology to handle API callings. This leads to very different code styles produced by different developers for similar APIs.

This template project provides a universal guide for creating Java Client SDK for these 3rd-party HTTP-based APIs.

## Client Overview

To create a client for HTTP based APIs, it requires a Http Client connector and a HttpMessage codec engine. We can use a client class as the entry of the Client SDK.

```java
public class SampleClient{
    <HttpClient Connector>,
    <Codec>
}

```

### Http Client Connector

A HTTP Client Connector is used to send the client request to the remote APIs, and receive the response result from the remote APIs, the progress is a bit complex and related to the details of the HTTP protocol.

When building the API Client, choosing a mature HttpClient library is a smart decision.

* In the initial stage, create a HttpClient instance and set up the connection properties, eg. connecting timeout, read timeout, etc., logging of request and response, and global headers for authorization, etc.
* Use the HttpClient instance to interact with the target APIs.

There are several popular options in the Java communities.

- [OkHttp](https://square.github.io/okhttp/) is a modern lightweight HttpClient, and it is very popular in Java and Android communities.
- [Apache HttpComponents](https://hc.apache.org/) is a classic HttpClient, version 5 is refactored and embraces the changes in the latest Java. Apache HttpComponents is widely used in open-source Java projects.
- [JDK 11 HttpClient](https://docs.oracle.com/en%2Fjava%2Fjavase%2F11%2Fdocs%2Fapi%2F%2F/java.net.http/java/net/http/HttpClient.html) is a completely new HttpClient added to the JDK core system since Java 11. This means using JDK 11 HttpClient will not introduce extra dependencies.

### HttpMessage Codecs

Besides the HTTP Client Connector, we need to process the data to the acceptable format of the remote APIs and also need to convert the received data to readable form for us.

Nowadays most APIs use JSON as the transport format.

- Before sending the request data to the remote server, serialize it to JSON string.
- After receiving the response data from remote APIs, deserialize it to the expected type form.

We can design a general-purpose interface for these use cases.

```java
@FunctionalInterface
public interface JsonEncoder {
    public String encode(Object obj);
}

@FunctionalInterface
public interface JsonDecoder {
    <T> T decode(String json, Class<T> clazz);
}
```

The Client should provide a default implementation for these interfaces, eg. using Jackson ObjectMapper.

```java
class ObjectMapperEncoder implements JsonEncoder {
    private final ObjectMapper objectMapper;

    public ObjectMapperEncoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String encode(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}

class ObjectMapperDecoder implements JsonDecoder {
    private final ObjectMapper objectMapper;

    public ObjectMapperDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T decode(String json, Class<T> clazz) {
        return objectMapper.readValue(json, clazz);
    }
}
```

These simple interfaces allow you to switch between different JSON codecs in the classpath.

In Java communities, there are several popular JSON projects available to process JSON serialization and deserialization.

- [Jackson ObjectMapper](https://github.com/FasterXML/jackson) is very popular in Java backend development.
- [Gson](https://github.com/google/gson) is a simple and lightweight lib and is also popular in Java communities.
- Jakarta JSON-B specification provides a standard API to serialize and deserialize JSON data, the implementation projects include [Eclipse Yasson](https://projects.eclipse.org/projects/ee4j.yasson) and [Apache Johnzon](https://johnzon.apache.org/).

When the Client is ready, the end developers can customize their implementations to replace the default ones.

In summary, to simplify the usage, we use the Client class as the entry of this SDK, and at least it should contain a configurable HttpClient connector and Codec.

For the extra configuration properties, we can use a Configuration POJO or apply the Builder pattern to assemble the configuration for this client.

## Design Guideline

Assume there is an existing API service named **hello**, and to simplify the work, let's assume it only provides a simple API as the following.

| Endpoint                           | Request                           | Response                     | Description |
| ---------------------------------- | --------------------------------- | ---------------------------- | ----------- |
| `GET /greetings`                   | accept a request parameter `name`, empty request body | return response body as json  `{"content":..., "createdAt":...}` | A Greetings API Sample            |

The API service base URL is `http://localhost:8080/api`.

We will use this simple API as an example to describe the naming and project structure when writing a Java Client SDK for it.


### Define Project Structure

Define a new package as the base package for the further new codes, and in the root of the package add a new class named `HelloClient`. 

Make sure the top package name and client class name match the API service name.
    
```bash 
|-io
    |-etip
        |-sdk
            |-hello
                |-HelloClient.java
```

Here we select OkHttp as the HTTP client connector and Jackson ObjectMapper for JSON processing. We create a `JsonCodec` to wrap the `JsonEncoder` and `JsonDecoder` interfaces.

```java
public class HelloClient {

    private OkHttpClient httpClient;
    private JsonCodec codecs;
    private String baseUri;
    ...
}
```
And the following is the content of the `JsonCodec`.

```java
public class JsonCodec {
    private JsonEncoder encoder;
    private JsonDecoder decoder;
}
```

To accept the external configuration parameters, in `HelloClient` we can introduce a `Builder` pattern.

```java
public class HelloClient {

    private OkHttpClient httpClient;
    private JsonCodec codecs;
    private String baseUri;

    public OkHttpClient httpClient() {
        return httpClient;
    }

    public JsonCodec codecs() {
        return codecs;
    }

    public String baseUri() {
        return baseUri;
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
                        .decoder(new ObjectMapperDecoder(objectMapper))
                        .encoder(new ObjectMapperEncoder(objectMapper))
                        .build();
            }


            client.baseUri = this.baseUri;
            client.httpClient = this.httpClient;
            client.codecs = this.codecs;
            return client;
        }
    }
}

```
Using a Builder pattern, we keep the `HelloClient` clean and move all complex work to the `Builder.build()` method. 

1. Always set a default HttpClient and Codec instances if the end users do not customize them. And allow the users to use their custom instance instead.
2. Accept all configuration parameters via the Builder class, keep the `HelloClient` as clean as possible, and make sure there are no tedious properties in `HelloClient`. In the above example, the `secretKey` is only used to initialize a HttpClient instance and never used in further API interaction. However, we will need to use `baseUri` to assemble the target URL in the API interaction.
3. Once the `HelloClient` is built completely, make sure the properties are read only. 

Alternatively, using Factory Method pattern is also good for creating a `HelloClient`` instance.

The following examples are some dummy codes.

```java
public class HelloClient{
    private OkHttpClient httpClient;
    private JsonCodec codecs;
    private String baseUri;

    static HelloClient create(HelloClientConfiguration config){

    }
} 
```

The difference is moving the complexity to the static `create` method, and using a simple `HelloClientConfiguration` POJO to accept configurable parameters.

### Interact with APIs

We can group the API callings into different interfaces according to the endpoint convention or functionality, for example, `catelogs`, `payments`, `orders`, etc. It is easy to maintain in the future.

We still use above the greeting APIs in the hello API service as an example. 

Create new package `greetings` that stand for the API group under the root package `io.etip.sdk.hello`. We will categorize all classes related to greetings APIs into this package.

Then create an interface `GreetingsApi` to list all interactions. NOTE, we use the `Api` for the postfix of this interface.

```java
public interface GreetingsApi {

    GetGreetingResponse getGreeting(GetGreetingRequest getGreetingRequest);
}
```
Create `GetGreetingRequest` and `GetGreetingResponse` POJOs to wrap the request and response data. 

```java
public record GetGreetingRequest(String name) {
    // add validation to the constructor
    public GetGreetingRequest {

        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
    }
}
```

According to the API docs, add validations in the `Request` constructor.

NOTE, we use the `Request` and `Response` for the postfix of these POJOs.

The implementation of `GreetingsApi` is located in a subpackage of this group, here it is `io.etip.sdk.hello.greetings.impl`, the implementation class is named `GreetingsApiImpl`.

```java
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
```

The implementation class accepts the `HelloClient` as the constructor parameters, then it can use its `HttpClient` and `Codec` to implement the method `getGreeting(...)` and handle API callings.

We can use custom exceptions to wrap different exception cases in the API response. Create a common parent exception class in the root package `io.etip.sdk.hello`, make it named `HelloException` and extend from `RuntimeException`. 

Create a subclass of `HelloException` for every API failed response, and catch all useful error information from API response in the exception. Here we create a dummy `GreetingFailedException` as an example.

```java
class GreetingsFailedException extends HelloException{

}
```

Let's return to `HelloClient`, and add a method as the entry of calling greetings API.

```java
public class HelloClient{
    //...

    public GreetingsApi greetings() {
        return new GreetingsApiImpl(this);
    }
}    
```

### Client Usage Examples

Let's have a look from an end user's view how to use the client SDK we just developed. 

Firstly build a `HelloClient`. 

```java
HelloClient client = HelloClient.newBuilder()
    .secretKey("my-secret-key")
    .baseUri("http://localhost:8080")
    .build();
```

If you want to customize the HttpClient and Codec.

```java
var httpClient = new OkHttpClient.Builder()
    .authenticator((route, response) -> {
        return response.request().newBuilder().header("Authorization", "my-secret-key").build();
    })
    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    .build();
JsonCodec jsonCodec = JsonCodec.newBuilder()
    .decoder(new GsonDecoder(gson))
    .encoder(new GsonEecoder(gson))
    .build();
HelloClient client = HelloClient.newBuilder()
    .httpClient(httpClient)
    .codecs(jsonCodec)
    .baseUri("http://localhost:8080")
    .build();
```
Here we add a logging interceptor to the HttpClient, and use Gson to process JSON data.

Then call the APIs like this.

```java
var response = client.greetings().getGreeting(new GetGreetingRequest("Hantsy"));
```

## Getting Started

As a Client SDK developer, follow these steps to start a new Java Client SDK project.

* Go to https://github.com/eTipio/client-sdk-template, click **Use this template** button in the top right area, and create your fork repository.
* Choose your HttpClient and JSON libs, and clean the dependencies.
* Read the target API docs carefully.
* Rename the project build name to `<service>-sdk-java`, eg. `tabapay-sdk-java`
* Rename the top package and entry `Client` class name.
* Repeat `write`, `test`,  `refactor` steps to contribute your codes.
* Some API services provide a sandbox environment or mock server to verify your codes and try to write integration tests for testing your codes against the environment close to the real world.
* For those APIs that do not have a testing environment, try to create a local mock server via OkHttp test server or WireMock.