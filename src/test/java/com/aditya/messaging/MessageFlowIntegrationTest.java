package com.aditya.messaging;

import com.aditya.messaging.consumer.MessageProcessor;
import com.aditya.messaging.producer.MessagePublisher;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * End-to-end test against an in-process Kafka broker: real producer, real listener,
 * real retry and dead-letter handling.
 */
@SpringBootTest
@EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@TestPropertySource(properties = {
        "app.messaging.topic=messages-it",
        "app.messaging.partitions=1",
        "app.messaging.retry-interval-ms=100",
        "app.messaging.max-retries=1"
})
class MessageFlowIntegrationTest {

    private static final String DEAD_LETTER_TOPIC = "messages-it.DLT";

    @Autowired
    private MessagePublisher publisher;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @SpyBean
    private MessageProcessor processor;

    @Test
    void publishedMessageIsConsumed() {
        publisher.publish("hello kafka");

        verify(processor, timeout(15_000)).process(argThat(event -> "hello kafka".equals(event.content())));
    }

    @Test
    void messageThatKeepsFailingIsRetriedThenDeadLettered() {
        doThrow(new IllegalStateException("simulated processing failure"))
                .when(processor).process(argThat(event -> event != null && "poison".equals(event.content())));

        publisher.publish("poison");

        // One original attempt + max-retries (1) = 2 calls before giving up.
        verify(processor, timeout(15_000).times(2)).process(argThat(event -> "poison".equals(event.content())));

        Map<String, Object> props = KafkaTestUtils.consumerProps("dlt-verifier", "false", broker);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(props).createConsumer()) {
            // The DLT is created by the application (KafkaAdmin), not declared on @EmbeddedKafka,
            // so subscribe directly; consumerProps() defaults to reading from the earliest offset.
            consumer.subscribe(List.of(DEAD_LETTER_TOPIC));
            ConsumerRecord<String, String> deadLetter =
                    KafkaTestUtils.getSingleRecord(consumer, DEAD_LETTER_TOPIC, Duration.ofSeconds(20));

            assertThat(deadLetter.value()).contains("poison");
            assertThat(deadLetter.headers().lastHeader(KafkaHeaders.DLT_EXCEPTION_MESSAGE)).isNotNull();
        }
    }
}
