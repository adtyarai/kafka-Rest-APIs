# kafka-Rest-APIs

A proof-of-concept Spring Boot service demonstrating Kafka integration: a REST endpoint publishes messages to a Kafka topic, and a `@KafkaListener` consumes them.

Built to get hands-on with Spring's Kafka abstractions — explicit producer/consumer factory configuration, programmatic topic creation, and the producer/consumer round trip.

## Tech stack

- Java 17
- Spring Boot 2.6.6 (Spring Web)
- Spring for Apache Kafka
- Maven (wrapper included)

## What it does

- `POST /api/v1/messages` accepts a JSON body `{ "message": "..." }` and publishes the message to the `adityaTopic` topic via `KafkaTemplate`
- `KafkaListeners` consumes from `adityaTopic` (consumer group `groupId`) and logs each message
- `KafkaTopicConfig` creates the topic programmatically with `TopicBuilder`
- `KafkaProducerConfig` / `KafkaConsumerConfig` build the producer and consumer factories explicitly rather than relying solely on Spring Boot auto-configuration

## Project structure

```
Config/      KafkaProducerConfig, KafkaConsumerConfig, KafkaTopicConfig
Controller/  MessageController — the publish endpoint
Listeners/   KafkaListeners — the consumer
Record/      MessageRequest — request payload (Java record)
```

## Running locally

**Prerequisites:** Java 17 and a Kafka broker reachable at `localhost:9092`.

1. Start Kafka locally (Docker or a local install). The broker address is configured in `src/main/resources/application.properties`:
   ```
   spring.kafka.bootstrap-servers=localhost:9092
   ```
2. Run the app:
   ```
   ./mvnw spring-boot:run
   ```
3. Publish a message:
   ```
   curl -X POST http://localhost:8080/api/v1/messages \
     -H "Content-Type: application/json" \
     -d '{"message":"hello kafka"}'
   ```
4. The consumer logs the received message to stdout.

## Notes

This is a learning/POC project, not a production service — there's no error handling, retry logic, dead-letter topic, schema registry, or authentication. The goal was to understand the Spring Kafka producer/consumer lifecycle end to end.
