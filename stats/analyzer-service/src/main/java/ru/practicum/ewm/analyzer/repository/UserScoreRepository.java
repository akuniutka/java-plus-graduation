package ru.practicum.ewm.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.analyzer.model.UserScore;

import java.util.Optional;

public interface UserScoreRepository extends JpaRepository<UserScore, Long> {

    Optional<UserScore> findByEventIdAndUserId(long eventId, long userId);
}
