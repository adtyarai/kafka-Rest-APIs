# Kafka Messaging Service

[![CI](https://github.com/adtyarai/kafka-Rest-APIs/actions/workflows/ci.yml/badge.svg)](https://github.com/adtyarai/kafka-Rest-APIs/actions/workflows/ci.yml)

A Spring Boot 3 service that accepts messages over REST, publishes them to Kafka as typed JSON events, and consumes them with a listener that retries failures and routes messages it can't process to a dead-letter topic.

## How it works

```
POST /api/v1/messages ──► MessagePublisher ──► [messages] ──► MessageListener ──► MessageProcessor
                                                                   │
                                                     fails after retries
                                                                   ▼
                                                           [messages.DLT]
```

1. `MessageController` validates the request and returns **202 Accepted** with a message ID. It's 202 rather than 200 because processing happens asynchronously, after the response is sent.
2. `MessagePublisher` wraps the content in a `MessageEvent` (ID, content, timestamp) and sends it with the event ID as the record key.
3. `MessageListener` receives the event and hands it to `MessageProcessor`, which holds the business logic, kept apart from Kafka plumbing so it can be tested and replaced on its own.
4. If processing throws, `DefaultErrorHandler` retries it (1 s apart, 2 retries by default). When retries run out, `DeadLetterPublishingRecoverer` publishes the event to `messages.DLT`, with the exception details in its headers, so one bad message doesn't block the partition or get silently dropped.

## Design notes

- **Boot auto-configuration** builds the producer and consumer factories from `application.yml`. The only custom beans are the topics and the error-handling policy.
- **Idempotent producer** (`enable.idempotence=true`, `acks=all`), so broker retries don't create duplicates.
- **`ErrorHandlingDeserializer`** wraps the JSON deserializer, so a malformed payload becomes a handled error instead of a poison pill that crashes the consumer on every poll.
- **Trusted packages** are restricted to the model package for JSON deserialization.
- **Typed configuration**: topic name, partition count and retry policy live in a validated `@ConfigurationProperties` record (`app.messaging.*`).
- **Constructor injection** and feature packages throughout.

## API

### `POST /api/v1/messages`

```json
{ "message": "hello kafka" }
```

| Outcome | Status |
|---|---|
| Queued for processing | `202 Accepted` with `{ "id": "...", "status": "ACCEPTED" }` |
| Blank or over 1000 characters | `400 Bad Request` (problem detail) |

## Running locally

**Prerequisites:** Java 17+, Docker.

```bash
# Start a single-node Kafka (KRaft mode, no ZooKeeper)
docker compose up -d

# Run the service
./mvnw spring-boot:run

# Publish a message
curl -i -X POST http://localhost:8080/api/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"message":"hello kafka"}'
```

The consumer logs each processed message. Point at another broker with `KAFKA_BOOTSTRAP_SERVERS`.

## Tests

```bash
./mvnw verify
```

- `MessageFlowIntegrationTest` starts an **embedded Kafka broker** and checks the full flow: a published message is consumed, and a message that keeps failing is retried the configured number of times and then lands on the dead-letter topic with exception headers.
- `MessageControllerTest` covers the 202 and 400 responses.

## Project structure

```
src/main/java/com/aditya/messaging/
├── api/        MessageController, request/response records
├── producer/   MessagePublisher
├── consumer/   MessageListener, MessageProcessor
├── config/     KafkaConfig (topics, retry + DLT), MessagingProperties
└── model/      MessageEvent
```

## Tech stack

Java 17 · Spring Boot 3.3 · Spring for Apache Kafka · Apache Kafka 3.8 (KRaft) · JUnit 5 · Mockito · AssertJ · Embedded Kafka · GitHub Actions
