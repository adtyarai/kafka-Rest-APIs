package com.aditya.messaging.producer;

import com.aditya.messaging.config.MessagingProperties;
import com.aditya.messaging.model.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(MessagePublisher.class);

    private final KafkaTemplate<String, MessageEvent> kafkaTemplate;
    private final MessagingProperties properties;

    public MessagePublisher(KafkaTemplate<String, MessageEvent> kafkaTemplate, MessagingProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    /**
     * Publishes asynchronously. The event ID is used as the record key, so retries of
     * the same event land on the same partition and keep their ordering.
     */
    public MessageEvent publish(String content) {
        MessageEvent event = MessageEvent.of(content);
        kafkaTemplate.send(properties.topic(), event.id().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish message {}", event.id(), ex);
                    } else {
                        log.info("Published message {} to {}-{}@{}", event.id(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
        return event;
    }
}
