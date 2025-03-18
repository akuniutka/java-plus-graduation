package ru.practicum.ewm.analyzer.service;

import ru.practicum.ewm.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.analyzer.model.SimilarityScore;

import java.util.List;

public interface SimilarityScoreService {

    void updateSimilarityScore(SimilarityScore newScore);

    List<RecommendedEvent> getRecommendationsForUser(long userId, int maxResults);

    List<RecommendedEvent> findNewSimilarEvents(long requesterId, long sampleEventId, int maxResults);
}
