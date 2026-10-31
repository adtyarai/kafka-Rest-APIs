package com.aditya.messaging.consumer;

import com.aditya.messaging.model.MessageEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    private final MessageProcessor processor;

    public MessageListener(MessageProcessor processor) {
        this.processor = processor;
    }

    /**
     * Any exception thrown here is handled by the {@code DefaultErrorHandler} bean:
     * retried, then sent to the dead-letter topic.
     */
    @KafkaListener(topics = "${app.messaging.topic}")
    public void onMessage(MessageEvent event) {
        processor.process(event);
    }
}
