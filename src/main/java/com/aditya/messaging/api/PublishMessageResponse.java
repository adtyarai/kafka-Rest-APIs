package com.aditya.messaging.api;

import java.util.UUID;

public record PublishMessageResponse(UUID id, String status) {
}
