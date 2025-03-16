package ru.practicum.ewm.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.model.SimilarityScore;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
public class EventSimilarityMapperImpl implements EventSimilarityMapper {

    @Override
    public SimilarityScore mapToSimilarityScore(final EventSimilarityAvro similarity) {
        if (similarity == null) {
            return null;
        }
        final SimilarityScore score = new SimilarityScore();
        score.setEventAId(similarity.getEventA());
        score.setEventBId(similarity.getEventB());
        score.setScore(similarity.getScore());
        score.setTimestamp(similarity.getTimestamp());
        return score;
    }
}
