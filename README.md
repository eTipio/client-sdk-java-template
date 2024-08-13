# Java Client SDK Template for HTTP APIs

In the previous development, we faced the problem that a lot of third-party APIs did not provide a Java or Kotin specific client SDK for developers to simplify the API calling. We (the API end users) have to use our own familiar technology to handle API callings. Different guys in the same project could produce varied result of the similiar API callings.

This template project provides an universal guide for create Java Client SDK for these 3rd-party HTTP based APIs.

## Creating a Client

To create a client for HTTP based APIs, it requires a Http Client connector and a HttpMessage Encoding/Decoding engine. 

### Http Client Connector 

A Http Client Connector is used to send the client request to the remote APIs, and receive the response result from the remote APIs, the progress is a bit complex and related to the details of the HTTP protocol. When building the API client, choosing a mature HttpClient library is a smart decision. 

There are serveral popular options in the Java communities. 

* [OkHttp](https://square.github.io/okhttp/) is a morden ligthweight HttpClient, and it is evry popular in Java and Android communities. 
* [Apache HttpComponents](https://hc.apache.org/) is a classic HttpClient, the version 5 is refactored and embrace the changes in the latest Java. Apache HttpComponents is widely used in the open-source Java projects.
* [JDK 11 HttpClient](https://docs.oracle.com/en%2Fjava%2Fjavase%2F11%2Fdocs%2Fapi%2F%2F/java.net.http/java/net/http/HttpClient.html) Since Java 11, a completely new HttpClient is added in the JDK core system. Which means using JDK 11 HttpClient will not introduce extra dependencies.

### HttpMessage Codecs
Besides the Http Client Connector, we need to process the data to the acceptable format of the remote APIs and also need to convert the received data to readable form for us. 

Nowadays most APIs use JSON as the transport format.

* Before sending the request data to the remote server, serializes it to JSON string.
* After received the response data from remote APIs, deserializes it to the expected type form.

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

And provides an implementation solution using the popular JSON libs.
* [Jackson ObjectMapper](https://github.com/FasterXML/jackson) is very popular in the Java backend development.
* [Gson](https://github.com/google/gson) is a simple and lightweight lib and also popular in Java communities.
* Jakarta JSON-B specfication provides a standard API to serialize and desearialize JSON data, the implementation projects includes [Eclipse Yasson](https://projects.eclipse.org/projects/ee4j.yasson) and [Apache Johnzon](https://johnzon.apache.org/).

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

When the client is ready, developers can provide their own implemenations to override the default ones.

In the Client, we can use a Configuration POJO or using Builder pattern to assemble the configurable parameters.

## Specification

## Getting Started Guide


