package com.aditya.messaging.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Producer and consumer factories come from Spring Boot auto-configuration
 * (see application.yml). This class only adds what auto-configuration can't infer:
 * the topics, and the retry / dead-letter policy for failed messages.
 */
@Configuration
public class KafkaConfig {

    @Bean
    NewTopic messagesTopic(MessagingProperties properties) {
        return TopicBuilder.name(properties.topic())
                .partitions(properties.partitions())
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic deadLetterTopic(MessagingProperties properties) {
        // Same partition count as the source topic: the recoverer publishes to the same partition number.
        return TopicBuilder.name(properties.deadLetterTopic())
                .partitions(properties.partitions())
                .replicas(1)
                .build();
    }

    /**
     * Retries a failing message a fixed number of times, then publishes it to
     * {@code <topic>.DLT} instead of blocking the partition or dropping it.
     * Spring Boot wires this bean into the default listener container factory.
     */
    @Bean
    CommonErrorHandler errorHandler(KafkaTemplate<?, ?> kafkaTemplate, MessagingProperties properties) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        return new DefaultErrorHandler(recoverer,
                new FixedBackOff(properties.retryIntervalMs(), properties.maxRetries()));
    }
}
