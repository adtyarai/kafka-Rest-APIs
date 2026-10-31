package com.aditya.messaging.api;

import com.aditya.messaging.model.MessageEvent;
import com.aditya.messaging.producer.MessagePublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessagePublisher publisher;

    public MessageController(MessagePublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Returns 202 Accepted rather than 200/201: the message is queued for
     * asynchronous processing, not yet processed when the response is sent.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PublishMessageResponse publish(@Valid @RequestBody PublishMessageRequest request) {
        MessageEvent event = publisher.publish(request.message());
        return new PublishMessageResponse(event.id(), "ACCEPTED");
    }
}
