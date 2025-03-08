package ru.practicum.ewm.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.EndpointHitDto;
import ru.practicum.ewm.stats.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class StatsClientFallback implements StatsClient {

    @Override
    public void saveHit(final EndpointHitDto endpointHitDto) {
        log.warn("Cannot save endpoint hit to stats service: {}", endpointHitDto);
    }

    @Override
    public List<ViewStatsDto> getStats(final LocalDateTime start, final LocalDateTime end, final List<String> uris,
            final boolean unique) {
        log.warn("Cannot retrieve data from stats service: start = {}, end = {}, uris = {}, unique = {}", start, end,
                uris, unique);
        return List.of();
    }
}
