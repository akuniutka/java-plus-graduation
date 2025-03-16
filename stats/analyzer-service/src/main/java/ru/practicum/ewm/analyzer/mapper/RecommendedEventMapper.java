package ru.practicum.ewm.analyzer.mapper;

import ru.practicum.ewm.analyzer.message.RecommendedEventProto;
import ru.practicum.ewm.analyzer.model.RecommendedEvent;

import java.util.List;

public interface RecommendedEventMapper {

    RecommendedEventProto mapToDto(RecommendedEvent event);

    List<RecommendedEventProto> mapToDto(List<RecommendedEvent> events);
}
