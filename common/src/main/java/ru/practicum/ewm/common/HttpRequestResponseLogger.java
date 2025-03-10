package ru.practicum.ewm.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class HttpRequestResponseLogger {

    protected Logger log = LoggerFactory.getLogger(getClass());

    protected void logHttpResponse(final HttpServletRequest httpRequest, final Object body) {
        final String queryString = extractQueryString(httpRequest);
        log.info("Responded to {} {}{}: {}", httpRequest.getMethod(), httpRequest.getRequestURI(), queryString, body);
    }

    protected String extractQueryString(final HttpServletRequest httpRequest) {
        return Optional.ofNullable(httpRequest.getQueryString()).map(s -> "?" + s).orElse("");
    }
}
