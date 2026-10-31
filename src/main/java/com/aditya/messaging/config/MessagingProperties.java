package com.aditya.messaging.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed, validated configuration under {@code app.messaging}, so topic names and
 * retry settings live in one place instead of string literals across the code.
 */
@Validated
@ConfigurationProperties(prefix = "app.messaging")
public record MessagingProperties(
        @NotBlank String topic,
        @Min(1) int partitions,
        @Min(0) long retryIntervalMs,
        @Min(0) long maxRetries) {

    public String deadLetterTopic() {
        return topic + ".DLT";
    }
}
