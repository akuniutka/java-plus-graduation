package ru.practicum.ewm.request.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.common.HttpRequestResponseLogger;
import ru.practicum.ewm.request.dto.EventRequestStatusDto;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.service.RequestService;
import ru.practicum.ewm.request.dto.UpdateEventRequestStatusDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController extends HttpRequestResponseLogger {

    private final RequestService service;
    private final EventRequestDtoValidatorExtension eventRequestDtoValidatorExtension;

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.addValidators(eventRequestDtoValidatorExtension);
    }

    @GetMapping
    public List<RequestDto> getRequests(
            @PathVariable final long userId,
            @PathVariable final long eventId,
            final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest);
        final List<RequestDto> dtos = service.getRequests(userId, eventId);
        logHttpResponse(httpRequest, dtos);
        return dtos;
    }

    @PatchMapping
    public EventRequestStatusDto processRequests(
            @PathVariable final long userId,
            @PathVariable final long eventId,
            @RequestBody @Valid final UpdateEventRequestStatusDto updateDto,
            final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest, updateDto);
        final EventRequestStatusDto dto = service.processRequests(eventId, updateDto, userId);
        logHttpResponse(httpRequest, dto);
        return dto;
    }
}
