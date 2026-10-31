package com.aditya.messaging.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published to Kafka. Carries its own ID so consumers can de-duplicate
 * and so a message can be traced from the HTTP request through to the listener.
 */
public record MessageEvent(UUID id, String content, Instant createdAt) {

    public static MessageEvent of(String content) {
        return new MessageEvent(UUID.randomUUID(), content, Instant.now());
    }
}
