package ru.practicum.ewm.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.request.dto.EventRequestStatusDto;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.service.RequestService;
import ru.practicum.ewm.request.dto.UpdateEventRequestStatusDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
@Slf4j
public class PrivateRequestController {

    private final RequestService service;
    private final RequestMapper mapper;
    private final EventRequestDtoValidatorExtension validatorExtension;

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.addValidators(validatorExtension);
    }

    @GetMapping
    public List<RequestDto> findAllByInitiatorIdAndEventId(
            @PathVariable final long userId,
            @PathVariable final long eventId
    ) {
        log.info("Received request for participation requests: eventId = {}", eventId);
        final List<Request> requests = service.findAllByInitiatorIdAndEventId(userId, eventId);
        final List<RequestDto> dtos = mapper.mapToDto(requests);
        log.info("Responded with requested participation requests: eventId = {}", eventId);
        log.debug("Requested participation requests = {}", dtos);
        return dtos;
    }

    @PatchMapping
    public EventRequestStatusDto processRequests(
            @PathVariable final long userId,
            @PathVariable final long eventId,
            @RequestBody @Valid final UpdateEventRequestStatusDto updateDto
    ) {
        log.info("Received request to process participation requests: eventId = {}, status = {}, requests = {}",
                eventId, updateDto.status(), updateDto.requestIds());
        final EventRequestStatusDto dto = service.processRequests(eventId, updateDto, userId);
        log.info("Responded to participation requests processing request: eventId = {}, status = {}, requests = {}",
                eventId, updateDto.status(), updateDto.requestIds());
        log.debug("Processed participation requests = {}", dto);
        return dto;
    }
}
