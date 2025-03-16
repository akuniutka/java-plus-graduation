package ru.practicum.ewm.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.analyzer.model.UserScore;

import java.util.List;
import java.util.Optional;

public interface UserScoreRepository extends JpaRepository<UserScore, Long> {

    Optional<UserScore> findByEventIdAndUserId(long eventId, long userId);

    @Query("select u.eventId as eventId, sum(u.score) as score from UserScore u where u.eventId in :eventIds group by u.eventId")
    List<RecommendedEvent> getEventInteractionsCount(@Param("eventIds") List<Long> eventIds);
}
