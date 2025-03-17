package ru.practicum.ewm.analyzer.service;

import ru.practicum.ewm.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.analyzer.model.UserScore;

import java.util.List;

public interface UserScoreService {

    void updateUserScore(UserScore newScore);

    List<UserScore> findAllByUserId(long userId);

    List<RecommendedEvent> getEventInteractionsCount(List<Long> eventIds);
}
