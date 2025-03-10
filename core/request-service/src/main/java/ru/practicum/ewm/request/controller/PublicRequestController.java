package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.service.RequestService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
public class PublicRequestController {

    private final RequestService service;
    private final RequestMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto add(@PathVariable final long userId, @RequestParam long eventId) {
        log.info("Received request to add new participation request: requesterId = {}, eventId = {}", userId, eventId);
        final Request request = service.add(userId, eventId);
        final RequestDto dto = mapper.mapToDto(request);
        log.info("Responded with participation request added: id = {}, requesterId = {}, eventId = {}",
                dto.id(), dto.requester(), dto.event());
        log.debug("Participation request added = {}", dto);
        return dto;
    }

    @GetMapping
    public Collection<RequestDto> findAllByRequesterId(@PathVariable final long userId) {
        log.info("Received request for participation requests: requesterId = {}", userId);
        final List<Request> requests = service.findAllByRequesterId(userId);
        final List<RequestDto> dtos = mapper.mapToDto(requests);
        log.info("Responded with requested participation requests: requesterId = {}", userId);
        log.debug("Requested participation requests = {}", dtos);
        return dtos;
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto delete(@PathVariable final long userId, @PathVariable long requestId) {
        log.info("Received request to cancel participation request: id = {}", requestId);
        final Request request = service.cancel(userId, requestId);
        final RequestDto dto = mapper.mapToDto(request);
        log.info("Responded to participation request cancelling request: id = {}, status = {}", dto.id(), dto.status());
        log.debug("Cancelled participation request = {}", dto);
        return dto;
    }
}