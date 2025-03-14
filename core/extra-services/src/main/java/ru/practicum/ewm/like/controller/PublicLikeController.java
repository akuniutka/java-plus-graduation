package ru.practicum.ewm.like.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.like.service.LikeService;

@RestController
@RequestMapping("/events/{eventId}/like")
@RequiredArgsConstructor
@Slf4j
public class PublicLikeController {

    private static final String USER_HEADER = "X-EWM-USER-ID";

    private final LikeService service;

    @PutMapping
    public void add(@PathVariable final long eventId, @RequestHeader(USER_HEADER) final long userId) {
        log.info("Received request to add new like: eventId = {}, userId = {}", eventId, userId);
        service.add(eventId, userId);
        log.info("Processed request to add new like: eventId = {}, userId = {}", eventId, userId);
    }
}
