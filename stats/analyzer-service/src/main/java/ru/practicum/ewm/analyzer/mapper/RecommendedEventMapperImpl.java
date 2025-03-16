package ru.practicum.ewm.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.message.RecommendedEventProto;
import ru.practicum.ewm.analyzer.model.RecommendedEvent;

import java.util.List;

@Component
public class RecommendedEventMapperImpl implements RecommendedEventMapper {

    @Override
    public RecommendedEventProto mapToDto(final RecommendedEvent event) {
        if (event == null) {
            return null;
        }
        return RecommendedEventProto.newBuilder()
                .setEventId(event.getEventId())
                .setScore(event.getScore())
                .build();
    }

    @Override
    public List<RecommendedEventProto> mapToDto(final List<RecommendedEvent> events) {
        if (events == null) {
            return null;
        }
        return events.stream()
                .map(this::mapToDto)
                .toList();
    }
}
