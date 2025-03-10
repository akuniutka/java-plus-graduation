package ru.practicum.ewm.stats.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.stats.EndpointHitDto;
import ru.practicum.ewm.stats.model.ViewStats;
import ru.practicum.ewm.stats.ViewStatsDto;

import java.util.List;

@Component
public class StatsMapper {

    public EndpointHit mapToEndpointHit(final EndpointHitDto dto) {
        if (dto == null) {
            return null;
        }
        final EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(dto.app());
        endpointHit.setUri(dto.uri());
        endpointHit.setIp(dto.ip());
        endpointHit.setTimestamp(dto.timestamp());
        return endpointHit;
    }

    public ViewStatsDto mapToDto(final ViewStats viewStats) {
        if (viewStats == null) {
            return null;
        }
        return ViewStatsDto.builder()
                .app(viewStats.getApp())
                .uri(viewStats.getUri())
                .hits(viewStats.getHits())
                .build();
    }

    public List<ViewStatsDto> mapToDto(final List<ViewStats> viewStats) {
        if (viewStats == null) {
            return null;
        }
        return viewStats.stream()
                .map(this::mapToDto)
                .toList();
    }
}
