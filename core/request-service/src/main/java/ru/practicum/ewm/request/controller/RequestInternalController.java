package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.request.RequestService;
import ru.practicum.ewm.request.client.RequestOperations;
import ru.practicum.ewm.request.dto.RequestStats;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RequestInternalController implements RequestOperations {

    private final RequestService service;

    @Override
    public List<RequestStats> getConfirmedRequestStats(final Set<Long> eventIds) {
        log.info("Received request for participation request stats: eventId = {}", eventIds);
        final List<RequestStats> stats = service.getConfirmedRequestStats(eventIds);
        log.info("Responded with requested participation requests stats: eventId = {}", eventIds);
        log.debug("Requested stats = {}", stats);
        return stats;
    }
}
