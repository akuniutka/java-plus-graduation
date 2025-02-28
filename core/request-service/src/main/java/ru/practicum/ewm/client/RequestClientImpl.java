package ru.practicum.ewm.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.EventRequestStats;
import ru.practicum.ewm.request.RequestRepository;
import ru.practicum.ewm.request.RequestState;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestClientImpl implements RequestClient {

    private final RequestRepository requestRepository;

    @Override
    public List<EventRequestStats> getConfirmedRequests(final List<Long> eventIds) {
        return requestRepository.getRequestStats(eventIds, RequestState.CONFIRMED);
    }
}
