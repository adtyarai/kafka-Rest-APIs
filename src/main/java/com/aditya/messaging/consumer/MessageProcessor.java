package com.aditya.messaging.consumer;

import com.aditya.messaging.model.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Business handling for a consumed message, kept separate from the Kafka listener
 * so it can be tested and replaced without touching messaging plumbing.
 */
@Component
public class MessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    public void process(MessageEvent event) {
        log.info("Processing message {}: {}", event.id(), event.content());
    }
}
