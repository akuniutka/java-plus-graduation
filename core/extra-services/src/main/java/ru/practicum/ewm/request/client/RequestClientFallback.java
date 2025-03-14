package ru.practicum.ewm.request.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.request.dto.RequestStats;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class RequestClientFallback implements RequestClient {

    @Override
    public boolean existsByRequesterIdAndStatusConfirmed(final long requesterId) {
        log.warn("Cannot check confirmed participation request existence with request service: requesterId = {}",
                requesterId);
        return false;
    }

    @Override
    public List<RequestStats> getConfirmedRequestStats(final Set<Long> eventIds) {
        log.warn("Cannot retrieve data on participation requests from request service: event id = {}", eventIds);
        return List.of();
    }
}
