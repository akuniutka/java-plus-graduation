package ru.practicum.ewm.analyzer.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.analyzer.message.RecommendedEventProto;
import ru.practicum.ewm.analyzer.model.RecommendedEvent;

import java.util.List;

@Mapper
public interface RecommendedEventMapper {

    RecommendedEventProto mapToDto(RecommendedEvent event);

    List<RecommendedEventProto> mapToDto(List<RecommendedEvent> events);
}
