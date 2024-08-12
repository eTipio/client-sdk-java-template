package io.etip.sdk.hello.greetings;

import io.etip.sdk.hello.HelloException;

public class GreetingFailedException extends HelloException {
    public GreetingFailedException(String message) {
        super(message);
    }
}
