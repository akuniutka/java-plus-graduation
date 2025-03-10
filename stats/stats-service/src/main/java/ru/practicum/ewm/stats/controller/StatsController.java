package ru.practicum.ewm.stats.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.stats.EndpointHitDto;
import ru.practicum.ewm.stats.mapper.StatsMapper;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.stats.model.ViewStats;
import ru.practicum.ewm.stats.service.StatsService;
import ru.practicum.ewm.stats.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService service;
    private final StatsMapper mapper;

    @PostMapping("/hit")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addEndpointHit(@RequestBody @Valid final EndpointHitDto dto) {
        log.info("Received request to add new hit: app = {}, uri = {}, timestamp = {}", dto.app(), dto.uri(),
                dto.timestamp());
        log.debug("New hit = {}", dto);
        final EndpointHit hit = mapper.mapToEndpointHit(dto);
        service.addEndpointHit(hit);
        log.info("Responded with {} to add hit request: app = {}, uri = {}, timestamp = {}", HttpStatus.CREATED,
                hit.getApp(), hit.getUri(), hit.getTimestamp());
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getViewStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") final LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") final LocalDateTime end,
            @RequestParam(required = false) final List<String> uris,
            @RequestParam(defaultValue = "false") final boolean unique
    ) {
        log.info("Received request for view stats: start = {}, end = {}, uris = {}, unique = {}", start, end, uris,
                unique);
        final List<ViewStats> viewStats = service.getViewStats(start, end, uris, unique);
        final List<ViewStatsDto> dtos = mapper.mapToDto(viewStats);
        log.info("Responded with requested view stats: start = {}, end = {}, uris = {}, unique = {}", start, end, uris,
                unique);
        log.debug("Requested view stats = {}", dtos);
        return dtos;
    }
}
