package ru.practicum.ewm.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.analyzer.model.SimilarityScore;

import java.util.Optional;

public interface SimilarityScoreRepository extends JpaRepository<SimilarityScore, Long> {

    Optional<SimilarityScore> findByEventAIdAndEventBId(long eventAId, long eventBId);
}
