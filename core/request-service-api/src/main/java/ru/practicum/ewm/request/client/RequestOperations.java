package ru.practicum.ewm.request.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.request.dto.RequestStats;

import java.util.List;
import java.util.Set;

public interface RequestOperations {

    @GetMapping("/internal/requests/stats/confirmed")
    List<RequestStats> getConfirmedRequestStats(@RequestParam Set<Long> eventIds);
}
