package ru.practicum.ewm.aggregator.model;

import lombok.Getter;

import java.util.HashMap;

public class Event {

    @Getter
    private final long id;

    private final HashMap<Long, Float> userScores;
    private float totalScore;

    public Event(final long id) {
        this.id = id;
        this.userScores = new HashMap<>();
        this.totalScore = 0.0f;
    }

    public boolean hasUser(final long userId) {
        return userScores.containsKey(userId);
    }

    public void setUserScore(final long userId, final float score) {
        final Float oldScore = userScores.put(userId, score);
        if (oldScore == null) {
            totalScore += score;
        } else {
            totalScore += score - oldScore;
        }
    }

    public float getUserScore(final long userId) {
        return userScores.getOrDefault(userId, 0.0f);
    }

    public float getScore() {
        return totalScore;
    }
}
