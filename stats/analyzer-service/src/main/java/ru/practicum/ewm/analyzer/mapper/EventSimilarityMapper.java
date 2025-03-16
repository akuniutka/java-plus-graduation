package ru.practicum.ewm.analyzer.mapper;

import ru.practicum.ewm.analyzer.model.SimilarityScore;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface EventSimilarityMapper {

    SimilarityScore mapToSimilarityScore(EventSimilarityAvro similarity);
}
