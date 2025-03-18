package ru.practicum.ewm.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.analyzer.model.SimilarityScore;
import ru.practicum.ewm.analyzer.repository.SimilarityScoreRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimilarityScoreServiceImpl implements SimilarityScoreService {

    private final SimilarityScoreRepository repository;

    @Override
    public void updateSimilarityScore(final SimilarityScore score) {
        /*
         *  In order to avoid "N + 1 problem" when preparing recommendations for user, save 2 copies of similarity
         *  score:
         *    1) eventA, eventB -> score
         *    2) eventB, eventA -> score
         */
        List.of(score, reversedCopy(score)).forEach(newScore ->
                repository.findByEventAIdAndEventBId(newScore.getEventAId(), newScore.getEventBId()).ifPresentOrElse(
                        oldScore -> updateScoreAndSave(oldScore, newScore),
                        () -> saveSimilarityScore(newScore)
                ));
    }

    @Override
    public List<RecommendedEvent> getRecommendationsForUser(final long userId, final int maxResults) {
        final List<Long> similarEventIds = repository.findNewSimilarEvents(userId, maxResults).stream()
                .map(RecommendedEvent::getEventId)
                .toList();
        return repository.findAllWithPredictedScore(similarEventIds, userId);
    }

    @Override
    public List<RecommendedEvent> findNewSimilarEvents(final long requesterId, final long sampleEventId,
            final int maxResults
    ) {
        return repository.findNewSimilarEvents(requesterId, sampleEventId, maxResults);
    }

    private SimilarityScore reversedCopy(final SimilarityScore score) {
        final SimilarityScore reversed = new SimilarityScore();
        reversed.setEventAId(score.getEventBId());
        reversed.setEventBId(score.getEventAId());
        reversed.setScore(score.getScore());
        reversed.setTimestamp(score.getTimestamp());
        return reversed;
    }

    private void updateScoreAndSave(final SimilarityScore oldScore, final SimilarityScore newScore) {
        oldScore.setScore(newScore.getScore());
        oldScore.setTimestamp(newScore.getTimestamp());
        saveSimilarityScore(oldScore);
    }

    private void saveSimilarityScore(final SimilarityScore score) {
        final SimilarityScore savedScore = repository.save(score);
        log.info("Saved similarity score: id = {}, eventAId = {}, eventBId = {}, score = {}",
                savedScore.getId(), savedScore.getEventAId(), savedScore.getEventBId(), savedScore.getScore());
        log.debug("Similarity score saved = {}", savedScore);
    }
}
