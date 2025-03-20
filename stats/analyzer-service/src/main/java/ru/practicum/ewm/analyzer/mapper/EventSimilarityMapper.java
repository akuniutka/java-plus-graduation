package ru.practicum.ewm.analyzer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.analyzer.model.SimilarityScore;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Mapper
public interface EventSimilarityMapper {

    @Mapping(target = "eventAId", source = "eventA")
    @Mapping(target = "eventBId", source = "eventB")
    SimilarityScore mapToSimilarityScore(EventSimilarityAvro similarity);
}
