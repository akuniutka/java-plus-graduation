package ru.practicum.ewm.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.analyzer.model.UserScore;
import ru.practicum.ewm.analyzer.repository.UserScoreRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserScoreServiceImpl implements UserScoreService {

    private final UserScoreRepository repository;

    @Override
    public void updateUserScore(final UserScore newScore) {
        repository.findByEventIdAndUserId(newScore.getEventId(), newScore.getUserId()).ifPresentOrElse(
                oldScore -> updateScoreAndSave(oldScore, newScore),
                () -> saveUserScore(newScore)
        );
    }

    @Override
    public List<UserScore> findAllByUserId(final long userId) {
        return repository.findAllByUserId(userId);
    }

    @Override
    public List<RecommendedEvent> getEventInteractionsCount(final List<Long> eventIds) {
        return repository.getEventInteractionsCount(eventIds);
    }

    private void updateScoreAndSave(final UserScore oldScore, final UserScore newScore) {
        if (newScore.getScore() <= oldScore.getScore()) {
            log.debug("Max user's score not changed: eventId = {}, userId = {}, old score = {}, new score = {}",
                    newScore.getEventId(), newScore.getUserId(), oldScore.getScore(), newScore.getScore());
            return;
        }
        oldScore.setScore(newScore.getScore());
        oldScore.setTimestamp(newScore.getTimestamp());
        saveUserScore(oldScore);
    }

    private void saveUserScore(final UserScore score) {
        final UserScore savedScore = repository.save(score);
        log.info("Saved user score: id = {}, eventId = {}, userId = {}, score = {}", savedScore.getId(),
                savedScore.getEventId(), savedScore.getUserId(), savedScore.getScore());
        log.debug("User score saved = {}", savedScore);
    }
}
