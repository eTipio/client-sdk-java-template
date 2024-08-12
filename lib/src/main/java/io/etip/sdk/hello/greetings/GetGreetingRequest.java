package io.etip.sdk.hello.greetings;

public record GetGreetingRequest(String name) {
    // add validation to the constructor
    public GetGreetingRequest {

        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
    }
}
