package ru.practicum.ewm.aggregator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Builder
public record UserAction(

        long userId,
        long eventId,
        ActionType actionType,
        Instant timestamp
) {

    private static final float VIEW_WEIGHT = 0.4f;
    private static final float REGISTER_WEIGHT = 0.8f;
    private static final float LIKE_WEIGHT = 1.0f;

    @Getter
    @RequiredArgsConstructor
    public enum ActionType {
        VIEW(VIEW_WEIGHT),
        REGISTER(REGISTER_WEIGHT),
        LIKE(LIKE_WEIGHT);

        private final float score;
    }
}
