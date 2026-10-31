package com.aditya.messaging.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublishMessageRequest(@NotBlank @Size(max = 1000) String message) {
}
