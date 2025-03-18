package ru.practicum.ewm.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.analyzer.model.SimilarityScore;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SimilarityScoreRepository extends JpaRepository<SimilarityScore, Long> {

    Optional<SimilarityScore> findByEventAIdAndEventBId(long eventAId, long eventBId);

    @Query("select s.eventBId as eventId, s.score as score "
            + "from SimilarityScore as s "
            + "where s.eventAId = :sampleEventId "
            + "and s.eventBId not in (select u.eventId from UserScore as u where u.userId = :requesterId) "
            + "order by score desc "
            + "limit :maxResults")
    List<RecommendedEvent> findNewSimilarEvents(
            @Param("requesterId") long requesterId,
            @Param("sampleEventId") long sampleEventId,
            @Param("maxResults") int maxResults
    );

    @Query("select s.eventBId as eventId, max(s.score) as score "
            + "from SimilarityScore as s "
            + "where s.eventAId in (select u.eventId from UserScore as u where u.userId = :userId) "
            + "and s.eventBId not in (select u.eventId from UserScore as u where u.userId = :userId) "
            + "group by eventId "
            + "order by score desc "
            + "limit :maxResults")
    List<RecommendedEvent> findNewSimilarEvents(
            @Param("userId") long userId,
            @Param("maxResults") int maxResults
    );

    @Query("select s.eventAId as eventId, sum(u.score * s.score) / sum(s.score) as score "
            + "from SimilarityScore as s "
            + "inner join UserScore as u on u.userId = :userId and s.eventBId = u.eventId "
            + "where s.eventAId in :eventIds "
            + "group by eventId "
            + "order by score desc")
    List<RecommendedEvent> findAllWithPredictedScore(
            @Param("eventIds") Collection<Long> eventIds,
            @Param("userId") long userId
    );
}
