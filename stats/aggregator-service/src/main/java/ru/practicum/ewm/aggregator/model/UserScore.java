package ru.practicum.ewm.aggregator.model;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UserScore(

        long userId,
        long eventId,
        float score,
        Instant timestamp
) {

}
