package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.stats.model.ViewStats;
import ru.practicum.ewm.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    void addEndpointHit(EndpointHit endpointHit);

    List<ViewStats> getViewStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
