package ru.practicum.ewm.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.analyzer.model.SimilarityScore;
import ru.practicum.ewm.analyzer.model.UserScore;
import ru.practicum.ewm.analyzer.repository.SimilarityScoreRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimilarityScoreServiceImpl implements SimilarityScoreService {

    private final UserScoreService userScoreService;
    private final SimilarityScoreRepository repository;

    @Override
    public void updateSimilarityScore(final SimilarityScore newScore) {
        repository.findByEventAIdAndEventBId(newScore.getEventAId(), newScore.getEventBId()).ifPresentOrElse(
                oldScore -> updateScoreAndSave(oldScore, newScore),
                () -> saveSimilarityScore(newScore)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedEvent> findNewSimilarEvents(final long requesterId, final long sampleEventId,
            final int numberOfEvents
    ) {
        final Set<Long> visitedEventIds = userScoreService.findAllByUserId(requesterId).stream()
                .map(UserScore::getEventId)
                .collect(Collectors.toCollection(HashSet::new));
        return repository.findAllByEventIdOrderBySimilarityDesc(sampleEventId)
                .filter(recommendedEvent -> !visitedEventIds.contains(recommendedEvent.getEventId()))
                .limit(numberOfEvents)
                .toList();
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
