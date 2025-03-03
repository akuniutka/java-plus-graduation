package ru.practicum.ewm.subscription;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.common.HttpRequestResponseLogger;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/subscriptions")
@RequiredArgsConstructor
public class SubscriptionPrivateController extends HttpRequestResponseLogger {

    private static final boolean DEFAULT_ONLY_AVAILABLE = false;
    private static final int DEFAULT_PAGE_FROM = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final SubscriptionService subscriptionService;

    @PostMapping
    public void subscribe(@PathVariable final long userId,
                          @RequestParam final long initiatorId,
                          final HttpServletRequest request) {
        logHttpRequest(request);
        subscriptionService.subscribe(userId, initiatorId);
        logHttpResponse(request);
    }

    @GetMapping
    public List<EventShortDto> getEvents(
            @PathVariable final long userId,
            @Valid final EventFilter filter,
            final HttpServletRequest request) {
        final EventFilter filterWithDefaults = withDefaults(filter);
        final List<EventShortDto> dtos = subscriptionService.getEvents(userId, filterWithDefaults);
        logHttpResponse(request, dtos);
        return dtos;
    }

    @DeleteMapping("/{initiatorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable final long userId,
                            @PathVariable final long initiatorId,
                            final HttpServletRequest request) {
        logHttpRequest(request);
        subscriptionService.unsubscribe(userId, initiatorId);
        logHttpResponse(request);
    }

    private EventFilter withDefaults(final EventFilter filter) {
        return filter.toBuilder()
                .onlyAvailable(filter.onlyAvailable() == null ? DEFAULT_ONLY_AVAILABLE : filter.onlyAvailable())
                .from(filter.from() == null ? DEFAULT_PAGE_FROM : filter.from())
                .size(filter.size() == null ? DEFAULT_PAGE_SIZE : filter.size())
                .build();
    }
}
