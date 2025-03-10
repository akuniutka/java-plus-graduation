package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.ewm.exception.ParameterValidationException;
import ru.practicum.ewm.stats.model.ViewStats;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository repository;

    @Override
    @Transactional
    public void addEndpointHit(final EndpointHit endpointHit) {
        final EndpointHit savedEndpointHit = repository.save(endpointHit);
        log.info("Added new hit: id = {}, app = {}, uri = {}, timestamp = {}", savedEndpointHit.getId(),
                savedEndpointHit.getApp(), savedEndpointHit.getUri(), savedEndpointHit.getTimestamp());
        log.debug("Hit added = {}", savedEndpointHit);
    }

    @Override
    public List<ViewStats> getViewStats(final LocalDateTime start, final LocalDateTime end, final List<String> uris,
            final boolean unique) {
        if (end.isBefore(start)) {
            throw new ParameterValidationException("end", "must be after or equal to 'start'", end);
        }
        if (CollectionUtils.isEmpty(uris)) {
            if (unique) {
                return repository.getUniqueHits(start, end);
            } else {
                return repository.getHits(start, end);
            }
        } else {
            if (unique) {
                return repository.getUniqueHits(start, end, uris);
            } else {
                return repository.getHits(start, end, uris);
            }
        }
    }
}
