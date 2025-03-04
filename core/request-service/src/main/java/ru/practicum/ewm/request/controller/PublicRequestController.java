package ru.practicum.ewm.request.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.common.HttpRequestResponseLogger;
import ru.practicum.ewm.request.service.RequestService;
import ru.practicum.ewm.request.dto.RequestDto;

import java.util.Collection;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PublicRequestController extends HttpRequestResponseLogger {
    private final RequestService requestService;

    @GetMapping
    public Collection<RequestDto> get(@PathVariable final long userId, final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest);
        Collection<RequestDto> response = requestService.getAllRequestByUserId(userId);
        logHttpResponse(httpRequest, response);
        return response;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto save(@PathVariable final long userId, @RequestParam long eventId, final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest);
        final RequestDto requestDto = requestService.create(userId, eventId);
        logHttpResponse(httpRequest, requestDto);
        return requestDto;
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto delete(@PathVariable final long userId, @PathVariable long requestId, final HttpServletRequest request) {
        logHttpRequest(request);
        RequestDto requestDto = requestService.cancel(userId, requestId);
        logHttpResponse(request, requestDto);
        return requestDto;
    }
}