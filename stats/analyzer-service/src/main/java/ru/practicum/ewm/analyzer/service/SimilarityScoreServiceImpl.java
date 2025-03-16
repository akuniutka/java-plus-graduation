package ru.practicum.ewm.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.analyzer.model.SimilarityScore;
import ru.practicum.ewm.analyzer.repository.SimilarityScoreRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimilarityScoreServiceImpl implements SimilarityScoreService {

    private final SimilarityScoreRepository repository;

    @Override
    public void updateSimilarityScore(final SimilarityScore newScore) {
        repository.findByEventAIdAndEventBId(newScore.getEventAId(), newScore.getEventBId()).ifPresentOrElse(
                oldScore -> updateScoreAndSave(oldScore, newScore),
                () -> saveSimilarityScore(newScore)
        );
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
        log.debug("Similarity score saved = {}", score);
    }
}
