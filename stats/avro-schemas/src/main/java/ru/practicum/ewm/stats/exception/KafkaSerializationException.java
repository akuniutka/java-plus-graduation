package ru.practicum.ewm.stats.exception;

public class KafkaSerializationException extends RuntimeException {

    public KafkaSerializationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
