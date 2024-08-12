package io.etip.sdk.hello.greetings;

import java.time.LocalDateTime;

public record GetGreetingResponse(String message, LocalDateTime createdAt) {
}
