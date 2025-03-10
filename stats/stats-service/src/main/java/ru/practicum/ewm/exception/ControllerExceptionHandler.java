package ru.practicum.ewm.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;

@RestControllerAdvice
public class ControllerExceptionHandler extends BaseExceptionHandler {

    public ControllerExceptionHandler(final Clock clock) {
        super(clock);
    }
}
