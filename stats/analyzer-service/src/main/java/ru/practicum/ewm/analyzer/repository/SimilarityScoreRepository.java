package ru.practicum.ewm.analyzer.repository;

import jakarta.persistence.QueryHint;
import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.analyzer.model.SimilarityScore;

import java.util.Optional;
import java.util.stream.Stream;

public interface SimilarityScoreRepository extends JpaRepository<SimilarityScore, Long> {

    Optional<SimilarityScore> findByEventAIdAndEventBId(long eventAId, long eventBId);

    @Query("select s.eventAId + s.eventBId - :eventId as eventId, s.score as score from SimilarityScore s "
            + "where s.eventAId = :eventId or s.eventBId = :eventId "
            + "order by s.score desc")
    @QueryHints(@QueryHint(name = AvailableHints.HINT_FETCH_SIZE, value = "25"))
    Stream<RecommendedEvent> findAllByEventIdOrderBySimilarityDesc(@Param("eventId") long eventId);
}
